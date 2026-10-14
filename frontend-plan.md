# Plan Frontend — App web de Reservas (funcional)

> **Alcance de este plan:** aplicación web funcional que permite operar todo el sistema
> (usuarios, recursos, reservas, disponibilidad) consumiendo la API del backend.
> **Desarrollo independiente:** no requiere el backend terminado. Se desarrolla contra el
> **contrato congelado (§2, copiado aquí para trabajar sin dependencias)** usando capa mock;
> el acople real ocurre en la Fase F5. El plan espejo está en `backend-plan.md`.
> Idioma de docs y commits: **español**.

---

## 1. Decisiones de stack (propuesta)

| Decisión | Elección | Motivo |
|---|---|---|
| Framework | **React + TypeScript + Vite** | Estándar actual, arranque rápido, Node 22 ya disponible en el devcontainer |
| Routing | **React Router** | Navegación por vistas sin recargas |
| HTTP | `fetch` nativo + cliente tipado propio (`src/api/`) | Sin dependencias extra; el contrato es pequeño y estable |
| Estilos | CSS por componente (sin framework UI) | Suficiente para una app funcional de curso; cero fricción de versiones |
| Tests | **Vitest** (cliente API + lógica) + checklist manual por vista | Proporcional al alcance; sin E2E pesado |
| Ubicación | Carpeta `frontend/` en la raíz del repo | Backend y frontend conviven sin mezclarse; un solo repo, dos despliegues |
| Puertos | Frontend dev `http://localhost:5173`, API `http://localhost:8080` | El backend (v1) ya contempla CORS para `:5173` |

- Requiere añadir a `.gitignore`: `node_modules/`, `frontend/dist/`.
- Requiere añadir la extensión de VS Code correspondiente al `devcontainer.json` al tocarlo
  (rebuild posterior). Node/npm ya están en la imagen, no tocan el `Dockerfile`.

---

## 2. Contrato API congelado v1 (copia para desarrollo independiente)

> Fuente de verdad: `backend-plan.md §2`. Si cambia allí, actualizar esta sección primero.
> Base URL configurable por variable de entorno `VITE_API_URL` (defecto `http://localhost:8080`).
> Fechas ISO-8601. Errores siempre `{status, error, message}`.

### 2.1 Tipos (TypeScript, espejo de los DTOs)

```ts
type EstadoUsuario = "ACTIVO" | "INACTIVO";
type EstadoRecurso = "DISPONIBLE" | "MANTENIMIENTO" | "FUERA_DE_SERVICIO";
type EstadoReserva = "ACTIVA" | "CANCELADA" | "FINALIZADA";
type TipoRecurso = "LABORATORIO" | "AUDITORIO" | "SALA_REUNIONES" | "EQUIPO_AUDIOVISUAL" | "OTRO";

interface Usuario { id: number; nombre: string; documento: string; correo: string; estado: EstadoUsuario; }
interface Recurso { id: number; nombre: string; descripcion: string; tipo: TipoRecurso; capacidad: number; estado: EstadoRecurso; }
interface Reserva {
  id: number;
  usuario: { id: number; nombre: string; estado: EstadoUsuario };
  recurso: { id: number; nombre: string; estado: EstadoRecurso };
  fechaInicio: string; fechaFin: string; estado: EstadoReserva; fechaCreacion: string;
}
interface Disponibilidad { recursoId: number; desde: string; hasta: string; disponible: boolean; reservasSolapadas: Reserva[]; }
interface ApiError { status: number; error: string; message: string; }
```

### 2.2 Endpoints a consumir (16)

| Método | Endpoint | Uso en la UI |
|---|---|---|
| POST `/usuarios` | Crear usuario | Formulario usuarios |
| GET `/usuarios` | Listar | Tabla usuarios |
| GET `/usuarios/{id}` | Detalle | Vista detalle |
| PATCH `/usuarios/{id}/estado` `{estado}` | Activar/inactivar | Botón en tabla/detalle |
| POST `/recursos` | Crear recurso | Formulario recursos |
| GET `/recursos[?estado=][?tipo=]` | Listar + filtros | Tabla con filtros |
| GET `/recursos/{id}` | Detalle | Vista detalle |
| PUT `/recursos/{id}` | Modificar | Formulario edición |
| PATCH `/recursos/{id}/estado` `{estado}` | Cambiar estado | Botón/selector |
| GET `/recursos/{id}/disponibilidad?desde=&hasta=` | Consultar disponibilidad | Vista + apoyo al crear reserva |
| POST `/reservas` | Crear reserva | Formulario (pre-chequea disponibilidad) |
| GET `/reservas/{id}` | Detalle | Vista detalle |
| DELETE `/reservas/{id}` | Cancelar (→ CANCELADA) | Botón con confirmación |
| GET `/reservas/usuario/{usuarioId}[?estado=]` | Reservas por usuario | Pestaña/filtro |
| GET `/reservas/recurso/{recursoId}[?desde=][?hasta=]` | Reservas por recurso | Pestaña/filtro |
| GET `/reservas?fecha=yyyy-MM-dd` | Reservas por día | Vista agenda del día |

### 2.3 Catálogo de errores a mostrar (código → mensaje al usuario)

| `error` (HTTP) | Comportamiento UI esperado |
|---|---|
| `VALIDACION` (400) | Resaltar campos del formulario + mostrar `message` |
| `NO_ENCONTRADO` (404) | Vista "no encontrado" con enlace de retorno |
| `DUPLICADO` (409) | Aviso junto a documento/correo |
| `USUARIO_INACTIVO`, `RECURSO_EN_MANTENIMIENTO`, `RECURSO_FUERA_DE_SERVICIO`, `RESERVA_SOLAPADA`, `LIMITE_RESERVAS_ACTIVAS` (409) | Banner con el `message` del backend (texto ya en español) + sugerencia (elegir otro horario/recurso) |
| `RESERVA_FINALIZADA` (409) | Aviso "la reserva ya está cerrada, no se puede cancelar" |
| Fallo de red / API caída | Pantalla de error con reintento (distinguir de errores de negocio) |

---

## 3. Fases y TODOs verificables

### Fase F0 — Andamiaje (bloquea todo lo demás)

- [ ] F0.1 Crear app Vite + React + TS en `frontend/` (`npm create vite@latest frontend -- --template react-ts`),
      `npm install` + `npm run dev` levanta en `:5173`.
  - *Verificar:* `http://localhost:5173` muestra la app de ejemplo.
- [ ] F0.2 Estructura base: `src/api/` (cliente + tipos del §2.1), `src/pages/` (Usuarios, Recursos,
      Reservas, Disponibilidad), `src/components/` (tabla, formulario, banner de error, layout),
      `src/mocks/` (datos + cliente mock intercambiable por `VITE_USE_MOCKS=true`).
  - *Verificar:* `npm run build` compila sin errores de TS (`tsc --noEmit` en verde).
- [ ] F0.3 Cliente API: wrapper `fetch` que antepone `VITE_API_URL`, parsea `ApiError` en respuestas
      no-2xx y lanza error tipado `{status, error, message}`.
  - *Verificar:* test Vitest que simula un 409 `RESERVA_SOLAPADA` y aserta que el cliente lo propaga íntegro.
- [ ] F0.4 Router con 4 rutas (`/usuarios`, `/recursos`, `/reservas`, `/disponibilidad`) + layout con navegación.
  - *Verificar:* navegar entre las 4 rutas sin recarga y sin errores en consola.
- [ ] F0.5 `.gitignore` (node_modules, dist) y sección en `README.md` o `frontend/README.md`
      con `npm install / npm run dev / npm run build`.
  - *Verificar:* `git status` no muestra `node_modules` ni `dist`.

**DoD F0:** `npm run dev` + `npm run build` verdes; las 4 vistas existen contra mocks y muestran datos.

### Fase F1 — Vista Usuarios (filas 1–4 del §2.2)

- [ ] F1.1 Tabla de usuarios (GET) con estado visible + filtro ACTIVO/INACTIVO en cliente.
- [ ] F1.2 Formulario crear usuario (POST → 201): validación en cliente (obligatorios, formato correo)
      espejo del backend; error `DUPLICADO` junto al campo.
- [ ] F1.3 Detalle de usuario (GET {id}) + botón activar/inactivar (PATCH) con confirmación.
- [ ] F1.4 Manejo `NO_ENCONTRADO` (vista dedicada) y banner genérico para el resto.
  - *Verificar (contra mocks):* crear → aparece en tabla; inactivar → cambia estado; forzar 404/409 mock y ver la UI correspondiente; cero errores en consola.

**DoD F1:** ciclo completo de usuarios operable solo con la UI.

### Fase F2 — Vista Recursos (filas 5–10 del §2.2)

- [ ] F2.1 Tabla con filtros por estado y tipo (query params reales al backend, no solo cliente).
- [ ] F2.2 Formularios crear (POST) y editar (PUT) con validación (capacidad > 0, campos obligatorios).
- [ ] F2.3 Cambio de estado (PATCH) con selector + confirmación.
- [ ] F2.4 Vista disponibilidad: selector de recurso + rango fechas → muestra `disponible` y lista de
      reservas solapadas (GET disponibilidad).
  - *Verificar (contra mocks):* CRUD completo, filtros que cambian la URL/params, disponibilidad que
    muestra ambos casos (libre/ocupado).

**DoD F2:** ciclo completo de recursos + disponibilidad consultable por UI.

### Fase F3 — Vista Reservas (filas 11–16 del §2.2, el corazón funcional)

- [ ] F3.1 Formulario crear reserva (selectores de usuario ACTIVO y recurso DISPONIBLE, datetime-local
      inicio/fin): validación cliente (fin > inicio, inicio futuro) + **pre-chequeo de disponibilidad**
      antes de enviar; al 409 muestra el `message` del backend + sugerencia (otro horario/recurso).
- [ ] F3.2 Listados por usuario, por recurso y agenda del día (`?fecha=`) con filtro de estado.
- [ ] F3.3 Detalle + cancelar (DELETE con confirmación; tras cancelar muestra CANCELADA; caso
      `RESERVA_FINALIZADA` con aviso).
- [ ] F3.4 Regla visible de "máx. 3 activas": al crear para un usuario con 3 ACTIVAs, la UI anticipa
      el aviso (y respeta el 409 del backend como árbitro final).
  - *Verificar (contra mocks):* crear → aparece en agenda; solapar a propósito → banner con mensaje;
    cancelar → estado cambia; cada código del §2.3 mockeado se muestra donde corresponde.

**DoD F3:** una persona sin conocimientos técnicos puede reservar, consultar y cancelar solo con la UI.

### Fase F4 — Cierre QA frontend

- [ ] F4.1 Tests Vitest del cliente API (éxito + cada error del catálogo) y de validaciones de formularios.
  - *Verificar:* `npm test` verde.
- [ ] F4.2 Checklist manual por vista (documentado en `frontend/README.md`): sin errores de consola,
      estados de carga/vacío/error en cada tabla, formulario que no pierde datos ante un 409.
- [ ] F4.3 `npm run build` + previsualizar (`npm run preview`) sirviendo el `dist/` sin errores.

**DoD F4 (= DoD del frontend standalone):** build verde + tests verdes + checklist completo contra mocks.

### Fase F5 — Acople con el backend real (SOLO cuando B5 y F4 estén verdes)

- [ ] F5.1 Apuntar `VITE_API_URL=http://localhost:8080`, `VITE_USE_MOCKS=false`; backend levantado
      (`./gradlew bootRun`); verificar CORS (sin errores `Access-Control-Allow-Origin` en consola).
- [ ] F5.2 Smoke test integrado end-to-end con datos semilla: crear usuario → crear recurso →
      consultar disponibilidad → crear reserva → verla en agenda → cancelarla → verificar estados.
- [ ] F5.3 Recorrer los 8 disparadores de reglas desde la UI y confirmar que cada banner muestra el
      `message` real del backend.
- [ ] F5.4 Documentar en `README.md` el arranque conjunto (dos terminales: `bootRun` + `npm run dev`)
      y el flujo demo.

**DoD F5 (= DoD del sistema):** el smoke test pasa contra el backend real sin tocar código de ninguno
de los dos lados más allá de configuración (URLs/flags). Si algo falla, el defecto se clasifica:
¿el backend viola §2 o el frontend malinterpreta §2? — el contrato arbitra.

---

## 4. Orden de ejecución recomendado

F0 → F1 → F2 → F3 → F4 (todo contra mocks, en paralelo al backend). F5 solo tras B5+F4 verdes.
Commits en español, un commit por TODO completado cuando sea razonable.
