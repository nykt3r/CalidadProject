# Dev Container — CalidadProject (Java + Maven + JMeter)

Entorno de desarrollo reproducible para todo el equipo: **Java 21**, **Maven** y **Apache JMeter** empaquetados en un contenedor Docker, con integración nativa en VS Code.

## Herramientas incluidas

| Herramienta | Versión | Notas |
|---|---|---|
| JDK | Amazon Corretto **21** (`al2023`) | Base: Amazon Linux 2023 |
| Maven | **3.9.9** | Instalado en `/opt/maven` |
| JMeter | **5.6.3** | Instalado en `/opt/jmeter`, modo CLI (sin GUI) |
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
Clona el repositorio **dentro del sistema de archivos de WSL** (ej. `~/workspace/CalidadProject`) y no en `C:\...`. El I/O sobre `/mnt/c` es drásticamente más lento (afecta compilación Maven y ejecución de JMeter).

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
   - La primera vez compila la imagen (~2-4 min, descarga Maven y JMeter).
   - Siguientes veces arranca en segundos usando caché.
3. Al terminar verás en el log del contenedor la salida del `postCreateCommand`:

```
openjdk version "21.x.x" ...
Apache Maven 3.9.9 ...
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

## 5. Verificación rápida ("Hola, Mundo")

Hay un archivo de prueba en la raíz: `HolaMundo.java`.

```bash
javac HolaMundo.java && java HolaMundo
# Salida esperada: ¡Hola, Mundo desde CalidadProject!
rm HolaMundo.class   # limpiar artefacto
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

## 7. Solución de problemas

| Síntoma | Causa/Solución |
|---|---|
| Build falla con conflicto `curl` vs `curl-minimal` | Ya corregido: no volver a añadir `curl` a la lista de paquetes `dnf`; la imagen base trae `curl-minimal` con el binario completo necesario |
| Descarga lenta/fallida desde `archive.apache.org` | Reintentar; si persiste, cambiar las URLs del Dockerfile al mirror `dlcdn.apache.org` |
| Red corporativa con proxy | Añadir `HTTP_PROXY`/`HTTPS_PROXY` como build args en `devcontainer.json` y configurar el proxy en Docker Desktop |
| `permission denied` al usar docker (Linux) | Falta agregar tu usuario al grupo `docker` (ver sección 1) |
| Archivos creados con dueño incorrecto (Linux) | Ajustar `USER_UID`/`USER_GID` (ver sección 4) |
| Cambios en Dockerfile no se reflejan | Ejecutar `Dev Containers: Rebuild Container` |
