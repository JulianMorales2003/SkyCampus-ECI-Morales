<#
Crea en SonarQube el Quality Gate "SkyCampus Monferno" y se lo asigna al proyecto skycampus-v2.
Uso (PowerShell, con SonarQube arriba y el token en la variable de entorno):
    $env:SONAR_TOKEN = "<tu token>"
    .\scripts\configurar-quality-gate.ps1
El token NUNCA se escribe en este archivo.
#>
param(
    [string]$Host_ = "http://localhost:9000",
    [string]$Proyecto = "skycampus-v2",
    [string]$Gate = "SkyCampus Monferno"
)

if (-not $env:SONAR_TOKEN) { Write-Error "Define primero `$env:SONAR_TOKEN"; exit 1 }
$h = @{ Authorization = "Bearer $env:SONAR_TOKEN" }

function Post($ruta, $cuerpo) {
    Invoke-RestMethod -Method Post -Headers $h -Uri "$Host_/api/$ruta" -Body $cuerpo | Out-Null
}

# Si el gate ya existe, se recrea para que las condiciones queden exactamente como abajo
$existentes = (Invoke-RestMethod -Headers $h -Uri "$Host_/api/qualitygates/list").qualitygates.name
if ($existentes -contains $Gate) { Post "qualitygates/destroy" @{ name = $Gate } }
Post "qualitygates/create" @{ name = $Gate }

# metrica, operador (LT = menor que, GT = mayor que), umbral que hace fallar el gate
$condiciones = @(
    @("coverage",                 "LT", "85"),   # cobertura total menor a 85 %
    @("bugs",                     "GT", "0"),    # algun bug
    @("vulnerabilities",          "GT", "0"),    # alguna vulnerabilidad
    @("code_smells",              "GT", "0"),    # algun code smell
    @("sqale_index",              "GT", "30"),   # deuda tecnica mayor a 30 minutos
    @("duplicated_lines_density", "GT", "3")     # mas de 3 % de lineas duplicadas
)
foreach ($c in $condiciones) {
    Post "qualitygates/create_condition" @{ gateName = $Gate; metric = $c[0]; op = $c[1]; error = $c[2] }
}

Post "qualitygates/select" @{ gateName = $Gate; projectKey = $Proyecto }
Write-Host "Quality Gate '$Gate' creado y asignado a $Proyecto."
