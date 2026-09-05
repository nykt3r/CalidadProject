# Dev Container — CalidadProject (Java + Gradle + JMeter)

Entorno de desarrollo reproducible para todo el equipo: **Java 21**, **Gradle** y **Apache JMeter** empaquetados en un contenedor Docker, con integración nativa en VS Code.

## Herramientas incluidas

| Herramienta | Versión | Notas |
|---|---|---|
| JDK | Amazon Corretto **21** (`al2023`) | Base: Amazon Linux 2023 |
| Gradle | **9.7.1** | Instalado en `/opt/gradle` |
| JMeter | **5.6.3** | Instalado en `/opt/jmeter`, modo CLI (sin GUI) |
| Sonar Scanner CLI | **6.2.1** | Instalado en `/opt/sonar-scanner`. Cliente que envía tu código al servidor SonarQube |
| Git | Incluido en la imagen | — |

Usuario del contenedor: `vscode` (uid/gid 1000).

---

## 1. Requisitos previos según tu sistema operativo

| SO | Requisito |
|---|---|
| **Windows 10/11** | [WSL2](https://learn.microsoft.com/windows/wsl/install) + [Docker Desktop](https://www.docker.com/products/docker-desktop/) (backend WSL2) + VS Code + extensión [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) |
| **macOS (Intel/Apple Silicon)** | Docker Desktop + VS Code + extensión Dev Containers |
| **Linux** | Docker Engine (`sudo apt install docker.io` o equivalente) + VS Code + extensión Dev Containers. Añade tu usuario al grupo docker: `sudo usermod -aG docker $USER` (cierra sesión y vuelve a entrar) |

> La imagen base es multi-arquitectura: funciona de forma nativa en x86_64 y ARM64 (Apple Silicon incluido).

### Recomendación importante para Windows
Clona el repositorio **dentro del sistema de archivos de WSL** (ej. `~/workspace/CalidadProject`) y no en `C:\...`. El I/O sobre `/mnt/c` es drásticamente más lento (afecta compilación Gradle y ejecución de JMeter).

```powershell
# Desde PowerShell (abre el repo dentro de WSL)
wsl
cd ~/workspace && git clone <url-del-repo>
code .
```

---

## 2. Uso con VS Code (todos los sistemas operativos)

1. Abre la carpeta del proyecto en VS Code.
2. Ejecuta el comando `Dev Containers: Reopen in Container` (F1 o Ctrl+Shift+P).
   - La primera vez compila la imagen (~2-4 min, descarga Gradle y JMeter).
   - Siguientes veces arranca en segundos usando caché.
3. Al terminar verás en el log del contenedor la salida del `postCreateCommand`:

```
openjdk version "21.x.x" ...
Gradle 9.7.1 ...
Copyright (c) 1999-2024 The Apache Software Foundation   ← JMeter 5.6.3
```

Si eso aparece sin errores, el entorno está listo.

### Reconstruir el contenedor
Tras cambiar `Dockerfile` o `devcontainer.json`, ejecuta `Dev Containers: Rebuild Container`.

---

## 3. Uso sin VS Code (CLI)

Útil para CI o si prefieres terminal pura.

**Bash / WSL / macOS / Linux:**

```bash
docker build -t calidad-dev .devcontainer
docker run --rm -it -v "$(pwd)":/workspace -w /workspace calidad-dev bash
```

**PowerShell (Windows):**

```powershell
docker build -t calidad-dev .devcontainer
docker run --rm -it -v "${PWD}:/workspace" -w //workspace calidad-dev bash
```

---

## 4. Notas específicas por sistema operativo

### Windows
- Verifica que WSL2 esté activo: `wsl --status`.
- Usa `${PWD}` (no `$(pwd)`) en PowerShell al montar volúmenes.
- Configura saltos de línea consistentes antes del primer commit:
  ```bash
  git config --global core.autocrlf input
  ```
- Si Docker Desktop no arranca, confirma que usa el backend WSL2 (Settings → General).

### macOS
- Para pruebas de carga con JMeter ajusta recursos de Docker Desktop: Settings → Resources (recomendado: ≥4 GB RAM, ≥2 CPU).
- En Apple Silicon no requiere Rosetta: la imagen corre nativa en ARM64.

### Linux
- El contenedor crea el usuario `vscode` con **uid/gid 1000**. Si tu usuario local tiene otro uid, los archivos creados dentro del contenedor quedarán con dueño `1000`. Solución: pasa tus uid/gid en `.devcontainer/devcontainer.json`:
  ```json
  "build": {
    "args": {
      "JAVA_VERSION": "21",
      "USER_UID": "1001",
      "USER_GID": "1001"
    }
  }
  ```
  y reconstruye el contenedor.
- Rootless Docker es compatible sin configuración adicional.

---

## 5. Verificación rápida (proyecto Spring Boot)

El repo es un proyecto Spring Boot con Gradle. Para confirmar que el entorno compila y levanta:

```bash
./gradlew build            # compila + empaqueta (el wrapper usa Gradle 9.7.1)
./gradlew bootRun          # levanta la API en http://localhost:8080
```

Prueba el endpoint definido hasta el momento:

```bash
curl http://localhost:8080/usuarios
```

---

## 6. JMeter (modo headless)

El contenedor no incluye entorno gráfico: los planes se ejecutan en modo no-GUI (el estándar para pruebas de carga).

```bash
jmeter -n -t plan-de-pruebas.jmx \
       -l resultados.jtl \
       -e -o report/
```

- `-n`: modo no-GUI · `-l`: resultados crudos · `-e -o`: reporte HTML.
- Los archivos `.jmx` se editan como XML (texto) o con la GUI de JMeter instalada localmente fuera del contenedor.
- JMeter genera `jmeter.log` en el directorio actual al arrancar: es normal. Sugerido para `.gitignore`:
  ```
  jmeter.log
  *.jtl
  report/
  ```

---

## 7. SonarQube (análisis estático)

El setup respeta la filosofía del proyecto (**nada de herramientas en tu local**):
- El **servidor** SonarQube corre como contenedor *sidecar* definido en `docker-compose.yml` (raíz del repo): imagen oficial `sonarqube:26.8.0.126808-community` (versión fija) + PostgreSQL 16.
- El **scanner** (cliente que manda tu código al servidor) está instalado dentro de este devcontainer en `/opt/sonar-scanner`.

### 7.1 Levantar el servidor
```bash
docker compose up -d
```
- Dashboard: <http://localhost:9000>
- Primer acceso: usuario `admin`, password `admin` (el servidor te pedirá cambiarla).
- Detener sin borrar datos: `docker compose down`.
- Borrar datos por completo: `docker compose down -v`.

### 7.2 Conectividad devcontainer ↔ servidor
El devcontainer y el servidor son contenedores independientes. Para que el scanner alcance al servidor, usa `host.docker.internal` como host (no `localhost`):
```bash
sonar-scanner \
  -Dsonar.projectKey=calidad-project \
  -Dsonar.host.url=http://host.docker.internal:9000 \
  -Dsonar.login=<TOKEN>
```
> En Docker Desktop (`extra_hosts`) `host.docker.internal` funciona sin config. En Docker Engine de Linux debe añadirse `--add-host=host.docker.internal:host-gateway` al contenedor del devcontainer si el host por defecto no lo resuelve.

### 7.3 Crear un token
1. Entra en el dashboard (`:9000`) → **My Account** (esquina superior) → **Security** → **Tokens**.
2. Crea un token y pásalo con `-Dsonar.login=<TOKEN>` al scanner.

### 7.4 Verificación de carga (JSON no interactivo)
```bash
sonar-scanner --version   # dentro del devcontainer
```

> Para un análisis por defecto con `sonar-project.properties`, el scanner genera `.scannerwork/` en el directorio actual (ya ignorado por `.gitignore`).

---

## 8. Solución de problemas

| Síntoma | Causa/Solución |
|---|---|
| Build falla con conflicto `curl` vs `curl-minimal` | Ya corregido: no volver a añadir `curl` a la lista de paquetes `dnf`; la imagen base trae `curl-minimal` con el binario completo necesario |
| Descarga lenta/fallida desde `archive.apache.org` | Reintentar; si persiste, cambiar las URLs del Dockerfile al mirror `dlcdn.apache.org` |
| Red corporativa con proxy | Añadir `HTTP_PROXY`/`HTTPS_PROXY` como build args en `devcontainer.json` y configurar el proxy en Docker Desktop |
| `permission denied` al usar docker (Linux) | Falta agregar tu usuario al grupo `docker` (ver sección 1) |
| Archivos creados con dueño incorrecto (Linux) | Ajustar `USER_UID`/`USER_GID` (ver sección 4) |
| Cambios en Dockerfile no se reflejan | Ejecutar `Dev Containers: Rebuild Container` |
| Scanner no alcanza al servidor | Verificar que SonarQube esté arriba (`docker compose up -d`) y usar `host.docker.internal` (no `localhost`) como `sonar.host.url` (sección 7.2) |
| El contenedor no resuelve `host.docker.internal` (Linux) | Añadir `--add-host=host.docker.internal:host-gateway` al contenedor del devcontainer (sección 7.2) |
