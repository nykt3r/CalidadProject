Feature: Booking cancellation

  Scenario: Cancel an active booking
    When a client books a future slot 1 on resource 1 for user 1
    And a client cancels the created booking
    Then the booking response status must be 200
    And the booking field "estado" must be "CANCELADA"

  Scenario: Cancelled booking shows cancelled state
    When a client books a future slot 1 on resource 1 for user 1
    And a client cancels the created booking
    And a client requests the created booking by id
    Then the booking field "estado" must be "CANCELADA"

  Scenario: Cancelling twice is rejected (rule 6)
    When a client books a future slot 1 on resource 1 for user 1
    And a client cancels the created booking
    And a client cancels the created booking
    Then the booking response status must be 409
    And the booking error code must be "RESERVA_FINALIZADA"

  Scenario: Cancelling unknown booking returns not found
    When a client cancels the booking with id 9999
    Then the booking response status must be 404
    And the booking error code must be "NO_ENCONTRADO"

  Scenario: Unknown booking id returns not found
    When a client requests the booking with id 9999
    Then the booking response status must be 404
    And the booking error code must be "NO_ENCONTRADO"
