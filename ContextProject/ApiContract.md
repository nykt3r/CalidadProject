# API Contract — Endpoints Mínimos Esperados

> Los nombres pueden variar siempre que se conserve una estructura REST coherente.

## Usuarios

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/usuarios` | Crear usuario |
| GET | `/usuarios` | Consultar usuarios |
| GET | `/usuarios/{id}` | Consultar usuario por id |
| PATCH | `/usuarios/{id}/estado` | Cambiar estado del usuario |

## Recursos

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/recursos` | Crear recurso |
| GET | `/recursos` | Consultar recursos |
| GET | `/recursos/{id}` | Consultar recurso por id |
| PATCH | `/recursos/{id}/estado` | Cambiar estado del recurso |
| GET | `/recursos/{id}/disponibilidad` | Consultar disponibilidad de un recurso |

## Reservas

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/reservas` | Crear reserva |
| GET | `/reservas/{id}` | Consultar reserva por id |
| DELETE | `/reservas/{id}` | Cancelar reserva |
| GET | `/reservas/usuario/{usuarioId}` | Consultar reservas por usuario |
| GET | `/reservas/recurso/{recursoId}` | Consultar reservas por recurso |
