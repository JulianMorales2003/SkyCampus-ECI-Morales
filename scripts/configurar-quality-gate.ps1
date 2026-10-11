<#
Crea en SonarQube el Quality Gate del nivel indicado y se lo asigna al proyecto skycampus-v2.
Uso (PowerShell, con SonarQube arriba y el token en la variable de entorno):
    $env:SONAR_TOKEN = "<tu token>"
    .\scripts\configurar-quality-gate.ps1                    # nivel Monferno (por defecto)
    .\scripts\configurar-quality-gate.ps1 -Nivel Infernape   # nivel Enterprise
    .\scripts\configurar-quality-gate.ps1 -Nivel Infernape -PerfilComplejidad   # y marca metodos con complejidad > 10
El token NUNCA se escribe en este archivo.
#>
param(
    [ValidateSet("Monferno", "Infernape")]
    [string]$Nivel = "Monferno",
    [string]$Host_ = "http://localhost:9000",
    [string]$Proyecto = "skycampus-v2",
    [switch]$PerfilComplejidad   # ademas crea el perfil Java que marca los metodos con complejidad ciclomatica mayor a 10
)

if (-not $env:SONAR_TOKEN) { Write-Error "Define primero `$env:SONAR_TOKEN"; exit 1 }
$h = @{ Authorization = "Bearer $env:SONAR_TOKEN" }

function Post($ruta, $cuerpo) {
    Invoke-RestMethod -Method Post -Headers $h -Uri "$Host_/api/$ruta" -Body $cuerpo | Out-Null
}

# metrica, operador (LT = menor que, GT = mayor que), umbral que hace fallar el gate
if ($Nivel -eq "Infernape") {
    $Gate = "SkyCampus Infernape"
    $condiciones = @(
        @("line_coverage",            "LT", "85"),    # cobertura de lineas menor a 85 %
        @("branch_coverage",          "LT", "75"),    # cobertura de ramas menor a 75 %
        @("bugs",                     "GT", "0"),     # algun bug
        @("vulnerabilities",          "GT", "0"),     # alguna vulnerabilidad
        @("sqale_index",              "GT", "14"),    # deuda tecnica de 15 min o mas (la meta es menos de 15)
        @("duplicated_lines_density", "GT", "2.99")   # duplicacion de 3 % o mas (la meta es menos de 3 %)
    )
} else {
    $Gate = "SkyCampus Monferno"
    $condiciones = @(
        @("coverage",                 "LT", "85"),    # cobertura total menor a 85 %
        @("bugs",                     "GT", "0"),     # algun bug
        @("vulnerabilities",          "GT", "0"),     # alguna vulnerabilidad
        @("code_smells",              "GT", "0"),     # algun code smell
        @("sqale_index",              "GT", "30"),    # deuda tecnica mayor a 30 minutos
        @("duplicated_lines_density", "GT", "3")      # mas de 3 % de lineas duplicadas
    )
}

# Si el gate ya existe, se recrea para que las condiciones queden exactamente como arriba
$existentes = (Invoke-RestMethod -Headers $h -Uri "$Host_/api/qualitygates/list").qualitygates.name
if ($existentes -contains $Gate) { Post "qualitygates/destroy" @{ name = $Gate } }
Post "qualitygates/create" @{ name = $Gate }

foreach ($c in $condiciones) {
    Post "qualitygates/create_condition" @{ gateName = $Gate; metric = $c[0]; op = $c[1]; error = $c[2] }
}

# SonarQube agrega por su cuenta condiciones sobre codigo nuevo (new_*) a un gate recien creado.
# El reto de Infernape pide solo las 6 de la tabla, todas sobre el codigo completo: se quitan.
if ($Nivel -eq "Infernape") {
    try {
        $creado = Invoke-RestMethod -Headers $h -Uri "$Host_/api/qualitygates/show?name=$([uri]::EscapeDataString($Gate))"
        foreach ($c in ($creado.conditions | Where-Object { $_.metric -like "new_*" })) {
            Post "qualitygates/delete_condition" @{ id = $c.id }
        }
    } catch {
        Write-Warning "No se pudieron quitar las condiciones new_*; quitalas a mano en Quality Gates -> $Gate."
    }
}

Post "qualitygates/select" @{ gateName = $Gate; projectKey = $Proyecto }
Write-Host "Quality Gate '$Gate' creado y asignado a $Proyecto."

if ($PerfilComplejidad) {
    $PerfilNombre = "SkyCampus Infernape (Java)"
    $regla = "java:S1541"   # los metodos no deben ser demasiado complejos
    try {
        $perfiles = (Invoke-RestMethod -Headers $h -Uri "$Host_/api/qualityprofiles/search?language=java").profiles
        $perfil = $perfiles | Where-Object { $_.name -eq $PerfilNombre } | Select-Object -First 1
        if (-not $perfil) {
            $base = $perfiles | Where-Object { $_.name -eq "Sonar way" } | Select-Object -First 1
            Post "qualityprofiles/copy" @{ fromKey = $base.key; toName = $PerfilNombre }
            $perfil = (Invoke-RestMethod -Headers $h -Uri "$Host_/api/qualityprofiles/search?language=java").profiles |
                Where-Object { $_.name -eq $PerfilNombre } | Select-Object -First 1
        }
        $parametro = (Invoke-RestMethod -Headers $h -Uri "$Host_/api/rules/show?key=$regla").rule.params | Select-Object -First 1
        $cuerpo = @{ key = $perfil.key; rule = $regla; severity = "CRITICAL" }
        if ($parametro) { $cuerpo.params = "$($parametro.key)=10" }
        Post "qualityprofiles/activate_rule" $cuerpo
        Post "qualityprofiles/add_project" @{ language = "java"; qualityProfile = $PerfilNombre; project = $Proyecto }
        Write-Host "Perfil '$PerfilNombre' con $regla (complejidad maxima 10) asignado a $Proyecto."
    } catch {
        Write-Warning "No se pudo configurar el perfil de complejidad: $($_.Exception.Message)"
    }
}
