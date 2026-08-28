# Arquitectura

## Arquitectura mínima esperada

El proyecto deberá mantener como mínimo la siguiente separación por capas:

```
Controller
    ↓
Service
    ↓
Repository
    ↓
Base de datos
```

**No se permitirá** acceder directamente al repositorio desde los controladores.

## Estructura de paquetes recomendada

Se recomienda utilizar la siguiente organización de paquetes:

- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `exception`
- `mapper`
