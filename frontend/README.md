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

## Checklist manual por vista (F4.2, parcial F0)

- [ ] Navegar las 4 rutas sin recarga ni errores en consola.
- [ ] Cada tabla muestra estados de carga / vacío / error.
- [ ] Disponibilidad muestra ambos casos (libre / ocupado).
