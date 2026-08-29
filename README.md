# CalidadProject — API de Reservas de Espacios y Recursos

Proyecto de curso de aseguramiento de calidad: **API REST en Spring Boot** para administrar la
reserva de espacios y recursos compartidos (laboratorios, auditorios, salas de reuniones, equipos
audiovisuales). La documentación de requisitos vive en [`docs/`](docs/) y es la fuente de verdad.

> **Estado: en construcción.** Hoy existe la base del proyecto con un primer endpoint operativo
> (`GET /usuarios`). El resto de la API se está construyendo en fases.

---

## 1. Stack

| Componente | Versión |
|---|---|
| Java | 21 (Corretto) |
| Spring Boot | 4.1.1 |
| Gradle | 9.7.1 (wrapper) |
| Base de datos | H2 en memoria (`jdbc:h2:mem:reservas`) |
| JPA / Hibernate | incluido vía `spring-boot-starter-data-jpa` |
| Build | Groovy DSL (`build.gradle`) |

## 2. Estado actual (lo definido hasta el momento)

- **Build reproducible**: Gradle wrapper 9.7.1 en la raíz del repo (sin instalación global de Gradle).
- **Modelo de datos** (`entity`): `Usuario`, `Recurso`, `Reserva` + enums `EstadoUsuario`
  (`ACTIVO`/`INACTIVO`), `EstadoRecurso` (`DISPONIBLE`/`MANTENIMIENTO`/`FUERA_DE_SERVICIO`),
  `EstadoReserva` (`ACTIVA`/`CANCELADA`/`FINALIZADA`) y `TipoRecurso`.
- **Repositorios** (`repository`): `UsuarioRepository`, `RecursoRepository`, `ReservaRepository`
  (Spring Data JPA).
- **Capa de servicio y controlador de usuarios** (mínima): 
  - `UsuarioService` → `listarUsuarios()`
  - `UsuarioController` → `GET /usuarios` (consulta todos los usuarios).
- **Seed temporal**: `data.sql` inserta 2 usuarios demo al arrancar.

### Endpoints disponibles

| Método | Endpoint | Respuesta |
|---|---|---|
| GET | `/usuarios` | `200` con la lista de usuarios en JSON |

Ejemplo de respuesta:

```json
[
  {
    "id": 1,
    "nombre": "Ana García",
    "documento": "DOC-001",
    "correo": "ana@example.com",
    "estado": "ACTIVO"
  }
]
```

### Arquitectura por capas

```
HTTP → Controller → Service → Repository → (JPA/Hibernate) → H2
```

Los controladores **nunca** acceden al repository directamente (regla del proyecto).

## 3. Estructura del proyecto

```
.
├── build.gradle / settings.gradle      # configuración de Gradle (Spring Boot 4.1.1)
├── gradlew / gradle/wrapper/           # Gradle wrapper 9.7.1 (reproducible)
├── src/main/java/app/calidad/reservas/
│   ├── ReservasApplication.java        # punto de arranque (@SpringBootApplication)
│   ├── controller/                     # UsuarioController (GET /usuarios)
│   ├── service/                        # UsuarioService
│   ├── repository/                     # interfaces Spring Data JPA
│   └── entity/                         # entidades + enums
├── src/main/resources/
│   ├── application.yml                 # configuración (H2, puerto 8080, JPA)
│   └── data.sql                        # seed temporal (2 usuarios demo)
├── docs/                               # requisitos (fuente de verdad)
├── .devcontainer/                      # entorno reproducible (Java + Gradle + JMeter)
└── pipeline.yml                        # CI Azure DevOps (build/test/acceptanceTest/pitest)
```

## 4. Cómo replicarlo en tu entorno

### Requisitos
- **Opción A (recomendada)**: Docker + VS Code con la extensión *Dev Containers* (no necesitas
  instalar Java ni Gradle).
- **Opción B**: JDK 21 y acceso a Red (Gradle lo descarga el propio wrapper).
- **Opción C (sin Java local)**: Docker y la imagen del entorno (`calidad-dev`).

### Opción A — Devcontainer (GitHub Codespaces / VS Code)

1. Clona el repositorio **dentro de WSL** en Windows (el I/O en `/mnt/c` es lento).
2. Abre la carpeta en VS Code → `Dev Containers: Reopen in Container`.
3. Verifica el entorno: `java -version && gradle --version`.
4. Compila y levanta:

```bash
./gradlew build       # compila + empaqueta (primera vez descarga wrapper y dependencias)
./gradlew bootRun     # levanta la API en http://localhost:8080
```

5. Prueba el endpoint:

```bash
curl http://localhost:8080/usuarios
```

### Opción B — JDK local sin contenedor

Con Java 21 instalado (Gradle lo provee el wrapper):

```bash
./gradlew build
./gradlew bootRun
curl -s http://localhost:8080/usuarios | jq
```

### Opción C — Imagen Docker del entorno (sin Java en el host)

Si tienes Docker pero no Java, puedes construir la imagen del devcontainer y ejecutar la app
desde un volumen de caché de Gradle:

```bash
docker build -t calidad-dev .devcontainer

docker run --rm \
  -v "$PWD":/workspace \
  -v "$HOME/.cache/calidad-gradle":/tmp/gradle-home \
  -w /workspace -e GRADLE_USER_HOME=/tmp/gradle-home \
  calidad-dev bash -lc "./gradlew build"

docker run --rm -p 8080:8080 \
  -v "$PWD":/workspace \
  -v "$HOME/.cache/calidad-gradle":/tmp/gradle-home \
  -w /workspace -e GRADLE_USER_HOME=/tmp/gradle-home \
  calidad-dev bash -lc "./gradlew bootRun"
```

(El volumen de caché hace que la segunda ejecución no re-descargue Gradle ni las dependencias.)

## 5. Comandos útiles

| Comando | Qué hace |
|---|---|
| `./gradlew build` | Compila, corre tests y empaqueta el JAR |
| `./gradlew clean build` | Reconstrucción limpia |
| `./gradlew bootRun` | Levanta la API en `http://localhost:8080` |
| `./gradlew test` | Ejecuta solo las pruebas |
| `./gradlew bootJar` | Empaqueta `build/libs/reservas-api-0.1.0.jar` (ejecutable: `java -jar ...`) |
| `java -jar build/libs/reservas-api-0.1.0.jar` | Corre el artefacto sin Gradle |

### Artefactos generados (`build/`, no versionados)
- `build/libs/reservas-api-0.1.0.jar` → JAR autocontenido (app + Tomcat embebido).
- `build/reports/tests/test/` → informe HTML de pruebas.
- `build/test-results/test/` → resultados XML (los consume el pipeline CI).

## 6. Configuración (`src/main/resources/application.yml`)

| Opción | Valor | Nota |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:mem:reservas` | BD en memoria; los datos se pierden al reiniciar |
| `spring.jpa.hibernate.ddl-auto` | `update` | Hibernate crea/actualiza tablas al arrancar |
| `spring.jpa.defer-datasource-initialization` | `true` | Permite que `data.sql` corra tras crear el esquema |
| `server.port` | `8080` | Puerto de la API |

## 7. Limitaciones actuales (a resolver en fases siguientes)

- `GET /usuarios` devuelve **entidades directamente** (aún no hay `dto`/`mapper`).
- No existe `@RestControllerAdvice`: los errores no tienen formato unificado todavía.
- No se aplican aún las **8 reglas de negocio** ni control de concurrencia (docs/) — pendiente la
  capa de servicios completa.
- El resto de endpoints del contrato (`/usuarios/{id}`, estados, `/recursos`, `/reservas`,
  disponibilidad) no existen.
- `data.sql` (**seed temporal**) inserta los 2 usuarios en cada arranque; se retirará al definir
  la estrategia de datos real.
- En el pipeline CI, los stages `acceptanceTest` (Cucumber) y `pitest` fallarán hasta que se
  configuren esas fases.

---

## Referencias

- [`docs/`](docs/) — requisitos, reglas de negocio, contrato de API, modelo de datos.
- [`.devcontainer/README.md`](.devcontainer/README.md) — entorno reproducible + SonarQube + JMeter.
- [`pipeline.yml`](pipeline.yml) — pipeline de Azure DevOps.