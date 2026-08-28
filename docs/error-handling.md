# Manejo de Errores

La aplicación debe implementar manejo global de excepciones mediante `@RestControllerAdvice`.

Las respuestas de error deben seguir una estructura similar a:

```json
{
  "status": 409,
  "error": "RECURSO_NO_DISPONIBLE",
  "message": "El recurso ya se encuentra reservado para este horario"
}
```
