# Reto 06 — RF, RNF y priorización MoSCoW (SkyCampus MVP)

## Requerimientos funcionales (qué hace el sistema)

- **RF-01.** El sistema debe permitir al Operador de drones consultar los drones disponibles, mostrando de cada uno su id, su batería actual y su ubicación.
  - *Resultado observable:* aparece una lista con solo los drones que tienen `disponible = true`.
- **RF-02.** El sistema debe permitir al Solicitante registrar una solicitud de reparto indicando origen, destino y tipo de carga (SOBRE, CARPETA o LIBRO).
  - *Resultado observable:* el sistema muestra el código de la misión generada, con estado PENDIENTE.
- **RF-03.** El sistema debe permitir al Operador de drones asignar un drone disponible a una misión PENDIENTE.
  - *Resultado observable:* la misión pasa a estado EN_VUELO y ese drone deja de aparecer en la lista del RF-01.

## Requerimientos no funcionales (cómo debe ser el sistema)

Cada uno lleva un número, una condición y una forma de verificarlo.

| Código | Atributo | Requerimiento | Número | Condición | Cómo se verifica |
|--------|----------|---------------|--------|-----------|------------------|
| RNF-01 | Rendimiento | La consulta de drones disponibles (RF-01) debe mostrar su resultado rápido | Menos de 2 segundos | Con una flota de 5 drones, en al menos 95 % de las consultas | Medir el tiempo de 20 consultas seguidas; al menos 19 deben cumplir |
| RNF-02 | Usabilidad | Un operador sin capacitación previa debe poder asignar un drone a una misión pendiente (RF-03) sin ayuda | Menos de 2 minutos | Usando el panel del operador, con 3 personas que no conozcan el sistema | Prueba con cronómetro; las 3 personas deben terminar dentro del tiempo |
| RNF-03 | Mantenibilidad | El código del dominio y de los servicios debe estar probado y limpio | Al menos 80 % de cobertura de líneas y 0 problemas Blocker o Critical | En el build del proyecto | Reportes de JaCoCo y SonarQube |
