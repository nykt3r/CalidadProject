# Concurrencia — Regla 5 bajo carga

## Decisión

`ReservaService.crearReserva` carga el recurso con **bloqueo pesimista de escritura**
(`PESSIMISTIC_WRITE`, ver `RecursoRepository.findByIdForUpdate`) dentro de la
transacción del service. La comprobación de solapamiento (regla 5) es un
*check-then-act*: sin bloqueo, dos peticiones concurrentes para el mismo recurso
y horario podían leer "sin solapamiento" a la vez y persistir dos reservas
`ACTIVA` solapadas. Con el bloqueo, la segunda petición espera a que la primera
confirme; al reanudar, `findActivasSolapadas` ya ve la reserva confirmada y
responde `409 RESERVA_SOLAPADA`.

Se descartó el constraint único de BD + reintento porque el solapamiento es un
rango (`inicio < hasta AND fin > desde`), no una igualdad de columnas, y no se
expresa con un unique constraint portable.

## Evidencia

`ReservaConcurrenciaTest`: 10 hilos reservan el mismo recurso y horario a la vez
→ exactamente **1** reserva `ACTIVA` y **9** respuestas `409 RESERVA_SOLAPADA`.

## Límites conocidos (relevantes para JMeter / 500 usuarios)

- H2 en memoria, una sola JVM: el bloqueo es a nivel de fila de esta instancia;
  **no hay coordinación entre nodos** (sin clustering).
- El hilo en espera bloquea hasta que la primera transacción confirma; con
  muchas peticiones concurrentes sobre el mismo recurso el tiempo de espera
  crece (posible `LockTimeoutException` bajo saturación extrema).
- La BD se pierde al reiniciar (alcance actual: desarrollo; ver fase B6 del
  `backend-plan.md` para la migración a PostgreSQL, donde el mismo bloqueo
  pesimista sigue siendo válido con `SELECT ... FOR UPDATE`).
