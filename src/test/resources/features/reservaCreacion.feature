Feature: Booking creation

  Scenario: Happy path
    When a client books a future slot 1 on resource 1 for user 1
    Then the booking response status must be 201
    And the booking field "estado" must be "ACTIVA"

  Scenario: Get booking by id
    When a client books a future slot 1 on resource 1 for user 1
    And a client requests the created booking by id
    Then the booking response status must be 200
    And the booking field "estado" must be "ACTIVA"

  Scenario: Inactive user cannot book (rule 1)
    When a client books a future slot 1 on resource 1 for user 2
    Then the booking response status must be 409
    And the booking error code must be "USUARIO_INACTIVO"

  Scenario: Resource in maintenance cannot be booked (rule 2)
    When a client books a future slot 1 on resource 2 for user 1
    Then the booking response status must be 409
    And the booking error code must be "RECURSO_EN_MANTENIMIENTO"

  Scenario: Resource out of service cannot be booked (rule 3)
    When a client books a future slot 1 on resource 3 for user 1
    Then the booking response status must be 409
    And the booking error code must be "RECURSO_FUERA_DE_SERVICIO"

  Scenario: End before start is rejected (rule 4)
    When a client books resource 1 for user 1 from "2030-06-01T11:00:00" to "2030-06-01T09:00:00"
    Then the booking response status must be 400
    And the booking error code must be "VALIDACION"

  Scenario: Overlapping booking is rejected (rule 5)
    When a client books resource 1 for user 1 from "2026-10-20T10:00:00" to "2026-10-20T12:00:00"
    Then the booking response status must be 409
    And the booking error code must be "RESERVA_SOLAPADA"

  Scenario: Past booking is rejected (rule 7)
    When a client books a past slot on resource 1 for user 1
    Then the booking response status must be 400
    And the booking error code must be "VALIDACION"

  Scenario: More than three active bookings is rejected (rule 8)
    When a client registers the booking user "Test Limite" with document "TEST-LIM" and email "limite@example.com"
    And a client books future slot 1 on resource 1 for the registered user
    And a client books future slot 2 on resource 1 for the registered user
    And a client books future slot 3 on resource 1 for the registered user
    And a client books future slot 4 on resource 1 for the registered user
    Then the booking response status must be 409
    And the booking error code must be "LIMITE_RESERVAS_ACTIVAS"

  Scenario: Unknown user returns not found
    When a client books a future slot 1 on resource 1 for user 9999
    Then the booking response status must be 404
    And the booking error code must be "NO_ENCONTRADO"

  Scenario: Unknown resource returns not found
    When a client books a future slot 1 on resource 9999 for user 1
    Then the booking response status must be 404
    And the booking error code must be "NO_ENCONTRADO"

  Scenario: List bookings by user contains the seed booking
    When a client lists bookings of user 1
    Then the booking response status must be 200
    And the listed bookings must contain id 1

  Scenario: List bookings by user filtered by state
    When a client lists bookings of user 1 with state "ACTIVA"
    Then the booking response status must be 200
    And the listed bookings must contain id 1

  Scenario: List bookings by resource contains the seed booking
    When a client lists bookings of resource 1
    Then the booking response status must be 200
    And the listed bookings must contain id 1

  Scenario: List bookings by resource in range contains the seed booking
    When a client lists bookings of resource 1 from "2026-10-20T00:00:00" to "2026-10-21T00:00:00"
    Then the booking response status must be 200
    And the listed bookings must contain id 1

  Scenario: List bookings by date contains the seed booking
    When a client lists bookings on date "2026-10-20"
    Then the booking response status must be 200
    And the listed bookings must contain id 1
