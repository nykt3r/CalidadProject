# AGENTS.md

Proyecto de curso de aseguramiento de calidad: una API REST en Spring Boot para reservar
espacios/recursos (laboratorios, salas, equipos audiovisuales). La documentación está en
**español**; mantén la consistencia de idioma en docs y commits nuevos.

## Estado actual (importante)
La API Spring Boot está **en construcción** en la raíz del repo, con paquete base
`app.calidad.reservas` (testigo: `group = 'app.calidad'` en `build.gradle`). Existe:
- build: Spring Boot 4.1.1, Gradle 9.7.1 (wrapper), Java 21, H2 en memoria.
- `entity` (Usuario/Recurso/Reserva + enums EstadoUsuario/EstadoRecurso/EstadoReserva/TipoRecurso)
  y `repository` (Usuario/Recurso/ReservaRepository).
- `service`/`controller` **mínimos de usuarios**: `GET /usuarios` funcional y sin reglas de
  negocio (devuelve entidades, no DTOs). `data.sql` siembra 2 usuarios demo (seed **temporal**,
  requiere `spring.jpa.defer-datasource-initialization: true`).
El stage `acceptanceTest` (Cucumber) y `pitest` del pipeline **fallarán hasta** que se agreguen
esas capas; se habilitan en fases posteriores.

Falta construir: `dto`, `mapper`, `exception` (+`@RestControllerAdvice`), `service` con las 8
reglas de negocio, el resto de endpoints (`GET /usuarios/{id}`, PATCH estados, `/recursos`,
`/reservas`, disponibilidad) y pruebas. Mantener la arquitectura por capas indicada abajo.

- `.devcontainer/` — entorno reproducible (Java 21 Corretto, Gradle 9.7.1, JMeter 5.6.3, Node 22, openspec, sonar-scanner)
- `pipeline.yml` — pipeline de Azure DevOps (build/test/acceptanceTest/pitest vía Gradle, `projectDir: .`)
- `docs/*.md` — **la fuente de verdad de los requisitos** (ver abajo)
- `README.md` — guía de funcionamiento y réplica del estado actual.

## Requisitos / especificaciones
Los requisitos viven en `docs/` (en español):
- `project-context.md` — alcance + tecnología (Java 17+, Spring Boot REST)
- `business-rules.md` — 8 reglas obligatorias (usuario inactivo, recursos en mantenimiento/fuera
  de servicio, sin reservas solapadas, máximo 3 reservas activas por usuario, etc.)
- `api-contract.md` / `data-model.md` — contrato de endpoints + modelo de entidades con estados de enum exactos
- `error-handling.md` — `@RestControllerAdvice` global; cuerpo de error con forma `{status, error, message}`
- `quality-requirements.md` — tooling futuro: SonarQube, JUnit, Mockito, PIT, Postman, JMeter

La arquitectura es obligatoriamente por capas: Controller → Service → Repository (BD). **Nunca**
accedas al Repository desde un Controller. Paquetes: `controller, service, repository, entity, dto, exception, mapper`.

## Entorno de desarrollo
- Trabaja dentro del devcontainer (VS Code "Reopen in Container"); reconstruye tras cambios en el Dockerfile.
- Verifica la instalación: `java -version && gradle --version && jmeter --version && node -v && openspec --version`.
- Compila/empaqueta: `./gradlew build` (o `clean build`). Levanta la API: `./gradlew bootRun` (puerto 8080).
- JMeter es solo headless: `jmeter -n -t plan.jmx -l results.jtl -e -o report/`.

## Problemas comunes (gotchas)
- `.gitignore` ya cubre `**/build/`, `.gradle/`, `jmeter.log`, `*.jtl` y `report/`; no comitees artefactos generados.
- En Windows, prefiere trabajar dentro del filesystem de WSL — el I/O en `/mnt/c` es muy lento (afecta a Gradle/JMeter).
- No vuelvas a añadir `curl` a la lista de paquetes dnf del Dockerfile (rompe el build — la imagen base ya trae `curl-minimal`).
- Gradle no arranca sin `xargs`/`find`: la imagen base es mínima, por eso `findutils` (y `unzip`) están en el `dnf install` del Dockerfile; no los quites.
- Es un proyecto de enseñanza/QA, no de producción.
