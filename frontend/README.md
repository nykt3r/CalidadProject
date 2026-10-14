# Frontend — App web de Reservas

App React + TypeScript + Vite que opera el sistema (usuarios, recursos, reservas,
disponibilidad) contra el contrato API congelado v1 (`frontend-plan.md §2`).

## Arranque

```sh
npm install
npm run dev      # http://localhost:5173
npm run build    # compila (tsc + vite)
npm run preview  # sirve dist/
npm test         # tests Vitest (vitest run)
```

## Configuración

| Variable | Defecto | Uso |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8080` | Base URL del backend real |
| `VITE_USE_MOCKS` | `true` | `true` = cliente mock en memoria (F0–F4); `false` = backend real (F5) |

## Estructura

- `src/api/` — tipos del contrato (§2.1), cliente HTTP tipado, singleton `api`.
- `src/mocks/` — datos semilla + cliente mock (misma interfaz `ReservasApi`).
- `src/pages/` — Usuarios, Recursos, Reservas, Disponibilidad.
- `src/components/` — layout con navegación, banner de error.

## Checklist manual por vista (F4.2)

Verificado contra mocks (`VITE_USE_MOCKS=true`, `npm run dev` en `:5173`).
En cada vista: abrir la consola del navegador y confirmar cero errores.

- [ ] Navegación: las 4 rutas (`/usuarios`, `/recursos`, `/reservas`, `/disponibilidad`)
      cambian sin recarga; el indicador muestra `mock`.
- [ ] Usuarios: tabla con filtro Todos/ACTIVO/INACTIVO; crear aparece en la tabla;
      duplicar documento/correo marca el campo sin perder datos; detalle con
      activar/inactivar (con confirmación); id inexistente muestra "no encontrado".
- [ ] Recursos: filtros `?estado=`/`?tipo=` visibles en la URL; crear/editar con
      validación (capacidad > 0); cambio de estado con selector y confirmación;
      id inexistente muestra "no encontrado".
- [ ] Disponibilidad: selector de recurso + rango; caso libre muestra "Disponible";
      caso ocupado muestra "No disponible" con la tabla de solapadas.
- [ ] Reservas: crear muestra aviso anticipado con 3 activas y pre-chequea
      disponibilidad; solapar a propósito muestra el banner con el mensaje;
      cancelar cambia a CANCELADA; recancelar muestra el aviso `RESERVA_FINALIZADA`;
      modos por día / usuario / recurso con filtros.
- [ ] Formularios que no pierden datos ante un 409 (usuario, recurso, reserva).
- [ ] Estados de carga / vacío / error (con reintento) en cada tabla.
- [ ] `npm run build` + `npm run preview`: el `dist/` sirve sin errores.
