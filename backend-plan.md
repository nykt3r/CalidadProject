# Plan Backend — API de Reservas (alcance ampliado)

> **Alcance de este plan:** completar el backend REST en Spring Boot hasta un producto funcional.
> **Persistencia:** se continúa con **H2 en memoria** durante todo el desarrollo; la migración a
> una BD real (PostgreSQL) queda planificada como fase futura explícita (Fase B6), fuera del
> alcance actual.
> **Desarrollo independiente:** este plan no depende del frontend. El acoplamiento posterior se hace
> exclusivamente contra el **contrato congelado (§2)**. El plan espejo está en `frontend-plan.md`.
> Idioma de docs y commits: **español**.

---

## 1. Punto de partida (ya existe, no rehacer)

- Entidades `Usuario`, `Recurso`, `Reserva` + enums (`EstadoUsuario`, `EstadoRecurso`, `EstadoReserva`,
  `TipoRecurso`) — alineados a `docs/data-model.md`.
- Repositories Spring Data JPA con métodos orientados a reglas (`countByUsuarioIdAndEstado`,
  `findByRecursoIdAndEstado`) + índice `idx_reservas_recurso_fechas` — aún sin consumir.
- `GET /usuarios` funcional (devuelve entidades, sin DTOs ni reglas).
- Seed temporal `data.sql` con 2 usuarios (`Ana García` ACTIVO, `Luis Pérez` INACTIVO); la feature
  Cucumber `usuarioService.feature` depende de exactamente esos 2 usuarios.
- Infra QA: `azure-pipeline.yml` (build → test → acceptanceTest → pitest), PIT al 100 % sobre
  `service`, Cucumber con JUnit Platform, SonarQube vía compose + scanner en el devcontainer.
- Restricciones vigentes (`AGENTS.md`, `docs/architecture.md`): capas
  Controller → Service → Repository (nunca Repository desde Controller); paquetes
  `controller, service, repository, entity, dto, exception, mapper`; layout no estándar de
  `src/test` (`steps/`, `runners/` cuelgan de `src/test`, ver `sourceSets` en `build.gradle`).

---

## 2. Contrato API congelado v1 (fuente de verdad para el acople)

Base URL en desarrollo: `http://localhost:8080`. JSON con fechas ISO-8601 (`yyyy-MM-dd'T'HH:mm:ss`).
Cualquier cambio a esta sección debe replicarse en `frontend-plan.md §2` antes de implementarse.

### 2.1 DTOs

```jsonc
// UsuarioResponse
{ "id": 1, "nombre": "Ana García", "documento": "DOC-001",
  "correo": "ana@example.com", "estado": "ACTIVO" }
// UsuarioRequest (POST): { "nombre": "...", "documento": "...", "correo": "..." }
// CambioEstadoRequest (PATCH): { "estado": "INACTIVO" }

// RecursoResponse
{ "id": 1, "nombre": "Lab 101", "descripcion": "...", "tipo": "LABORATORIO",
  "capacidad": 30, "estado": "DISPONIBLE" }
// RecursoRequest (POST/PUT): { "nombre": "...", "descripcion": "...",
  "tipo": "LABORATORIO", "capacidad": 30 }
// Tipos: LABORATORIO | AUDITORIO | SALA_REUNIONES | EQUIPO_AUDIOVISUAL | OTRO
// Estados recurso: DISPONIBLE | MANTENIMIENTO | FUERA_DE_SERVICIO

// ReservaResponse
{ "id": 1,
  "usuario": { "id": 1, "nombre": "Ana García", "estado": "ACTIVO" },
  "recurso": { "id": 1, "nombre": "Lab 101", "estado": "DISPONIBLE" },
  "fechaInicio": "2026-10-20T09:00:00", "fechaFin": "2026-10-20T11:00:00",
  "estado": "ACTIVA", "fechaCreacion": "2026-10-14T10:00:00" }
// ReservaRequest (POST): { "usuarioId": 1, "recursoId": 1,
//   "fechaInicio": "2026-10-20T09:00:00", "fechaFin": "2026-10-20T11:00:00" }
// Estados reserva: ACTIVA | CANCELADA | FINALIZADA

// DisponibilidadResponse (GET /recursos/{id}/disponibilidad?desde=...&hasta=...)
{ "recursoId": 1, "desde": "...", "hasta": "...",
  "disponible": true, "reservasSolapadas": [ /* ReservaResponse[] */ ] }
```

### 2.2 Endpoints (14 del contrato + 2 extensiones acordadas)

| # | Método | Endpoint | Éxito | Notas |
|---|---|---|---|---|
| 1 | POST | `/usuarios` | `201` UsuarioResponse | Valida nombre/documento/correo obligatorios, correo con formato, documento y correo únicos |
| 2 | GET | `/usuarios` | `200` UsuarioResponse[] | Ya existe; migrar a DTOs sin romper la feature Cucumber |
| 3 | GET | `/usuarios/{id}` | `200` UsuarioResponse | `404` + `NO_ENCONTRADO` si no existe |
| 4 | PATCH | `/usuarios/{id}/estado` | `200` UsuarioResponse | Body `CambioEstadoRequest` |
| 5 | POST | `/recursos` | `201` RecursoResponse | Estado inicial `DISPONIBLE`; capacidad > 0 |
| 6 | GET | `/recursos` | `200` RecursoResponse[] | Filtros opcionales `?estado=` y `?tipo=` |
| 7 | GET | `/recursos/{id}` | `200` RecursoResponse | `404` si no existe |
| 8 | PATCH | `/recursos/{id}/estado` | `200` RecursoResponse | Body `CambioEstadoRequest` |
| 9 | GET | `/recursos/{id}/disponibilidad?desde=&hasta=` | `200` DisponibilidadResponse | `disponible=false` si hay reservas ACTIVA solapadas o estado ≠ DISPONIBLE |
| 10 | POST | `/reservas` | `201` ReservaResponse | Aplica reglas 1–5, 7, 8; estado inicial `ACTIVA` |
| 11 | GET | `/reservas/{id}` | `200` ReservaResponse | `404` si no existe |
| 12 | DELETE | `/reservas/{id}` | `200` ReservaResponse | Cancelación lógica → estado `CANCELADA` (regla 6); no borra el registro |
| 13 | GET | `/reservas/usuario/{usuarioId}` | `200` ReservaResponse[] | Filtro opcional `?estado=` |
| 14 | GET | `/reservas/recurso/{recursoId}` | `200` ReservaResponse[] | Filtros opcionales `?desde=` `?hasta=` |
| E1 | PUT | `/recursos/{id}` | `200` RecursoResponse | **Extensión acordada** (lo exige `docs/functionalities.md` "Modificar recursos"; no está en el api-contract) |
| E2 | GET | `/reservas?fecha=yyyy-MM-dd` | `200` ReservaResponse[] | **Extensión acordada** (lo exige `docs/functionalities.md` "Consultar reservas por fecha") |

### 2.3 Catálogo de errores (cuerpo siempre `{status, error, message}`)

| Código `error` | HTTP | Cuándo |
|---|---|---|
| `VALIDACION` | 400 | Bean Validation fallida; regla 4 (fin ≤ inicio); regla 7 (inicio en el pasado); formato de fecha/estado inválido |
| `NO_ENCONTRADO` | 404 | id inexistente (usuario, recurso, reserva) |
| `DUPLICADO` | 409 | documento o correo de usuario ya registrados |
| `USUARIO_INACTIVO` | 409 | Regla 1 |
| `RECURSO_EN_MANTENIMIENTO` | 409 | Regla 2 |
| `RECURSO_FUERA_DE_SERVICIO` | 409 | Regla 3 |
| `RESERVA_SOLAPADA` | 409 | Regla 5 |
| `RESERVA_FINALIZADA` | 409 | Regla 6 (cancelar una FINALIZADA; cancelar una CANCELADA → también 409 con este código) |
| `LIMITE_RESERVAS_ACTIVAS` | 409 | Regla 8 (más de 3 ACTIVA por usuario) |

### 2.4 CORS (requisito para el acople con el frontend)

- El backend debe permitir origen `http://localhost:5173` (puerto dev de Vite) con métodos
  GET/POST/PUT/PATCH/DELETE. Configuración centralizada (no anotaciones sueltas por controller).

---

## 3. Fases y TODOs verificables

### Fase B0 — Base transversal (bloquea todo lo demás)

- [x] B0.1 Crear paquete `exception` con una excepción por código del §2.3
      (p. ej. `RecursoNoDisponibleException`, `ReservaSolapadaException`, `LimiteReservasException`,
      `RecursoNoEncontradoException`).
  - *Verificar:* `./gradlew compileJava` en verde.
- [x] B0.2 Crear `@RestControllerAdvice` global que mapee cada excepción a su HTTP + cuerpo
      `{status, error, message}`, más handlers para `MethodArgumentNotValidException` (400 VALIDACION)
      y `NoSuchElementException`/`EntityNotFound` (404 NO_ENCONTRADO).
  - *Verificar:* test de slice `@WebMvcTest` o MockMvc que fuerce un 404 y un 400 y aserte el cuerpo exacto.
- [x] B0.3 Crear paquetes `dto` (Request/Response del §2.1 con anotaciones `jakarta.validation`) y
      `mapper` (conversión entity ↔ DTO; el mapper de reserva aplana/resume usuario y recurso como en §2.1).
  - *Verificar:* tests unitarios de mappers (entity→DTO y DTO→entity, incluyendo nulos).
- [x] B0.4 Configurar CORS centralizado para `http://localhost:5173`.
  - *Verificar:* `curl -s -o /dev/null -w '%{http_code}' -X OPTIONS http://localhost:8080/usuarios -H 'Origin: http://localhost:5173' -H 'Access-Control-Request-Method: POST'` → `200` con `Access-Control-Allow-Origin`.
- [x] B0.5 Migrar `GET /usuarios` a DTOs manteniendo la respuesta compatible con la feature Cucumber
      (mismos campos y 2 usuarios del seed).
  - *Verificar:* `./gradlew test acceptanceTest` en verde.
- [x] B0.6 Ampliar `data.sql` con recursos demo (≥3, mezclando estados) y reservas demo coherentes,
      sin alterar los 2 usuarios existentes.
  - *Verificar:* arranque + `curl /recursos` muestra los recursos semilla.

**DoD B0:** `./gradlew clean build` verde; errores manuales vía curl devuelven `{status,error,message}`.

### Fase B1 — Usuarios (endpoints 1–4)

- [x] B1.1 `UsuarioService.crearUsuario` (valida duplicados documento/correo → `DUPLICADO 409`).
- [x] B1.2 `UsuarioService.obtenerPorId` (`NO_ENCONTRADO 404`) y `cambiarEstado` (PATCH).
- [x] B1.3 `POST /usuarios` (201), `GET /usuarios/{id}`, `PATCH /usuarios/{id}/estado` en el controller.
  - *Verificar por endpoint:* curl crea (201) → consulta (200) → inactiva (200) → consulta muestra INACTIVO; duplicar correo → 409 DUPLICADO; id inexistente → 404.
- [x] B1.4 Tests unitarios (Mockito) de cada método nuevo + feature Cucumber `usuario.feature`
      (crear, consultar por id, cambiar estado, errores 404/409).
  - *Verificar:* `./gradlew test acceptanceTest` verde.

**DoD B1:** contrato §2 filas 1–4 cumplido y probado.

### Fase B2 — Recursos (endpoints 5–9 + E1)

- [x] B2.1 `RecursoService` CRUD + `cambiarEstado` + filtros por estado/tipo.
- [x] B2.2 `PUT /recursos/{id}` (E1, modificación total) + controllers POST/GET/GET{id}/PATCH estado.
- [x] B2.3 Disponibilidad: `GET /recursos/{id}/disponibilidad?desde=&hasta=` — `disponible=false` si
      estado ≠ DISPONIBLE o existe reserva ACTIVA solapada (`inicio < hasta && fin > desde`).
  - *Verificar:* recurso en MANTENIMIENTO → disponible=false; recurso DISPONIBLE con reserva solapada → false + lista la reserva; rango libre → true.
- [x] B2.4 Tests unitarios + feature Cucumber `recurso.feature` (CRUD, estados, disponibilidad, 404/400).
  - *Verificar:* `./gradlew test acceptanceTest` verde.

**DoD B2:** contrato §2 filas 5–9 y E1 cumplidos y probados.

### Fase B3 — Reservas (endpoints 10–14 + E2, las 8 reglas)

- [x] B3.1 `ReservaService.crearReserva` aplicando en orden: existe usuario/recurso (404) →
      regla 4 (fin > inicio, 400) → regla 7 (inicio no pasado, 400) → regla 1 (usuario ACTIVO, 409) →
      reglas 2–3 (recurso DISPONIBLE, 409) → regla 8 (≤3 ACTIVAs del usuario, 409) →
      regla 5 (sin solapamiento ACTIVA, 409) → persiste en ACTIVA.
- [x] B3.2 `cancelarReserva`: ACTIVA → CANCELADA (200); FINALIZADA o CANCELADA → 409 `RESERVA_FINALIZADA`
      (regla 6). `DELETE` nunca borra físicamente.
- [x] B3.3 Consultas: `GET /reservas/{id}`, `/reservas/usuario/{usuarioId}?estado=`,
      `/reservas/recurso/{recursoId}?desde=&hasta=`, `/reservas?fecha=` (E2, reservas que intersectan ese día).
  - *Verificar por regla (curl):* una prueba que dispare cada código del §2.3 y devuelva el cuerpo exacto.
- [x] B3.4 Query de solapamiento en `ReservaRepository`
      (`findActivasSolapadas(recursoId, inicio, fin)`) + `countByUsuarioIdAndEstado` para la regla 8
      (método ya existente, ahora consumirlo).
- [x] B3.5 Tests unitarios por regla (uno por regla como mínimo, Mockito) + features Cucumber
      `reservaCreacion.feature` (camino feliz + reglas 1–5, 7, 8) y `reservaCancelacion.feature` (regla 6).
  - *Verificar:* `./gradlew test acceptanceTest` verde.

**DoD B3:** las 8 reglas implementadas, cada una con test que la dispare y cuerpo de error del catálogo.

### Fase B4 — Concurrencia (regla 5 bajo carga, base del escenario 500 usuarios)

- [x] B4.1 Endurecer `crearReserva` contra condiciones de carrera (decisión documentada en el README o
      en el código: bloqueo pesimista sobre el recurso + transacción, o constraint + reintento).
  - *Verificar:* test de concurrencia (hilos paralelos reservando el mismo recurso y horario) → exactamente 1 reserva ACTIVA y el resto 409.
- [x] B4.2 Documentar límites conocidos (H2 en memoria, sin clustering) como parte de la evidencia para
      la futura prueba JMeter de 500 usuarios.
  - *Verificar:* nota en README §7 o nuevo `docs/concurrency.md`.

**DoD B4:** imposible crear 2 reservas ACTIVA solapadas incluso en paralelo (test lo demuestra).

### Fase B5 — Cierre QA backend (puerta del pipeline)

- [ ] B5.1 PIT al 100 % en `service`: `./gradlew pitest` verde (umbrales ya configurados en `build.gradle`).
- [ ] B5.2 Colección Postman versionada (`postman/`) con los 16 endpoints + ejemplos de cada error del catálogo.
- [ ] B5.3 Plan JMeter (`jmeter/plan-reservas.jmx`, headless) con escenario de reservas concurrentes;
      `jmeter -n -t ... -l ... -e -o report/` genera el reporte (artefactos ya ignorados en `.gitignore`).
- [ ] B5.4 Pasada SonarQube limpia (compose arriba, scanner dentro del devcontainer) sin issues blocker.
- [ ] B5.5 Pipeline `azure-pipeline.yml` en verde end-to-end (build/test/acceptanceTest/pitest).
- [ ] B5.6 Actualizar `README.md` (tabla de endpoints, ejemplos curl, estado real) — el README actual
      dice "en construcción con 1 endpoint" y quedará obsoleto.

**DoD B5 (= DoD del backend):** pipeline verde + PIT 100 % + Postman/JMeter versionados + README al día.
El backend se declara integrable cuando un tercero, solo con §2 + Postman, puede operar las 8 reglas.

### Fase B6 — Migración a BD real (FUTURA, fuera de alcance actual)

> No implementar ahora. Queda esbozada para no tomar decisiones que la bloqueen
> (no usar SQL nativo H2-específico, no asumir IDs o fechas con comportamiento de H2).

- [ ] B6.1 Añadir servicio `db` (PostgreSQL 16) a `docker-compose.yml`, separado del `db` de SonarQube.
- [ ] B6.2 Perfiles Spring: `application-h2.yml` (dev/test actual) y `application-postgres.yml`
      (driver + `ddl-auto: validate` + credenciales por variables de entorno).
- [ ] B6.3 Introducir Flyway (o Liquibase): `V1__schema.sql` derivado de las entidades,
      `V2__seed.sql` con los datos demo; retirar `data.sql` + `defer-datasource-initialization`.
- [ ] B6.4 Migrar tests a Testcontainers (PostgreSQL) o mantener H2 solo para tests, documentando
      las divergencias aceptadas.
- [ ] B6.5 Re-ejecutar B5 completa contra PostgreSQL (incluido el test de concurrencia B4.1).

---

## 4. Orden de ejecución recomendado

B0 → B1 → B2 → B3 → B4 → B5. (B6 cuando se pida). Cada fase termina con `./gradlew clean build` verde
antes de pasar a la siguiente. Commits en español, un commit por TODO completado cuando sea razonable.
