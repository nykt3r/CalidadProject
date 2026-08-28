# AGENTS.md

Proyecto de curso de aseguramiento de calidad: una API REST en Spring Boot para reservar
espacios/recursos (laboratorios, salas, equipos audiovisuales). La documentación está en
**español**; mantén la consistencia de idioma en docs y commits nuevos.

## Estado actual (importante)
La aplicación real **aún no existe**. El repositorio solo contiene:
- `.devcontainer/` — entorno reproducible (Java 21 Corretto, Maven 3.9.9, JMeter 5.6.3, Node 22, openspec)
- `pipeline.yml` — pipeline de Azure DevOps (build/test/acceptanceTest/pitest vía Gradle)
- `HolaMundo.java` — solo prueba de humo (smoke test)
- `ContextProject/*.md` — **la fuente de verdad de los requisitos** (ver abajo)

No trates `pipeline.yml` como la realidad actual: compila un proyecto Gradle en `untitled1/`
que aún no existe. Primero construye la app Spring Boot real y luego reconcilia el pipeline.

## Requisitos / especificaciones
Los requisitos viven en `ContextProject/` (en español):
- `ProjectContext.md` — alcance + tecnología (Java 17+, Spring Boot REST)
- `BusinessRules.md` — 8 reglas obligatorias (usuario inactivo, recursos en mantenimiento/fuera
  de servicio, sin reservas solapadas, máximo 3 reservas activas por usuario, etc.)
- `ApiContract.md` / `DataModel.md` — contrato de endpoints + modelo de entidades con estados de enum exactos
- `ErrorHandling.md` — `@RestControllerAdvice` global; cuerpo de error con forma `{status, error, message}`
- `QualityRequirements.md` — tooling futuro: SonarQube, JUnit, Mockito, PIT, Postman, JMeter

La arquitectura es obligatoriamente por capas: Controller → Service → Repository (BD). **Nunca**
accedas al Repository desde un Controller. Paquetes: `controller, service, repository, entity, dto, exception, mapper`.

## Entorno de desarrollo
- Trabaja dentro del devcontainer (VS Code "Reopen in Container"); reconstruye tras cambios en el Dockerfile.
- Verifica la instalación: `java -version && mvn -version && jmeter --version && node -v && openspec --version`.
- JMeter es solo headless: `jmeter -n -t plan.jmx -l results.jtl -e -o report/`.

## Problemas comunes (gotchas)
- Todavía no existe `.gitignore`. `jmeter.log`, `*.jtl` y `report/` son generados y deberían ignorarse.
- En Windows, prefiere trabajar dentro del filesystem de WSL — el I/O en `/mnt/c` es muy lento (afecta a Maven/JMeter).
- No vuelvas a añadir `curl` a la lista de paquetes dnf del Dockerfile (rompe el build — la imagen base ya trae `curl-minimal`).
- Es un proyecto de enseñanza/QA, no de producción.
