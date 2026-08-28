# Modelo de Datos — Entidades Mínimas

## Usuario

Debe contener como mínimo:

| Campo | Descripción |
|---|---|
| id | Identificador único |
| nombre | Nombre del usuario |
| documento | Número de documento |
| correo | Correo electrónico |
| estado | Estado del usuario |

**Estados posibles:**
- `ACTIVO`
- `INACTIVO`

## Recurso

Debe contener:

| Campo | Descripción |
|---|---|
| id | Identificador único |
| nombre | Nombre del recurso |
| descripción | Descripción del recurso |
| tipo | Tipo de recurso |
| capacidad | Capacidad del recurso |
| estado | Estado del recurso |

**Estados posibles:**
- `DISPONIBLE`
- `MANTENIMIENTO`
- `FUERA_DE_SERVICIO`

## Reserva

Debe contener:

| Campo | Descripción |
|---|---|
| id | Identificador único |
| usuario | Usuario que realiza la reserva |
| recurso | Recurso reservado |
| fechaInicio | Fecha/hora de inicio de la reserva |
| fechaFin | Fecha/hora de fin de la reserva |
| estado | Estado de la reserva |
| fechaCreacion | Fecha de creación del registro |

**Estados mínimos:**
- `ACTIVA`
- `CANCELADA`
- `FINALIZADA`
