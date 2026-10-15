Feature: Resource management

  Scenario: Create resource
    When a client creates the resource "TEST-Lab" of type "LABORATORIO" with capacity 30
    Then the resource response status must be 201
    And the resource field "estado" must be "DISPONIBLE"

  Scenario: Get resource by id
    When a client creates the resource "TEST-Sala" of type "SALA_REUNIONES" with capacity 10
    And a client requests the created resource by id
    Then the resource response status must be 200
    And the resource field "nombre" must be "TEST-Sala"

  Scenario: Unknown resource id returns not found
    When a client requests the resource with id 9999
    Then the resource response status must be 404
    And the resource error code must be "NO_ENCONTRADO"

  Scenario: List resources filtered by state
    When a client lists resources with state "DISPONIBLE"
    Then the resource response status must be 200
    And the listed resources must contain "Lab 101"

  Scenario: Full update of a resource
    When a client creates the resource "TEST-Viejo" of type "OTRO" with capacity 5
    And a client fully updates the created resource to "TEST-Nuevo" with capacity 50
    Then the resource response status must be 200
    And the resource field "nombre" must be "TEST-Nuevo"
    And the resource field "estado" must be "DISPONIBLE"

  Scenario: Full update of unknown resource returns not found
    When a client fully updates the resource with id 9999
    Then the resource response status must be 404
    And the resource error code must be "NO_ENCONTRADO"

  Scenario: Change resource state
    When a client creates the resource "TEST-Estado" of type "AUDITORIO" with capacity 100
    And a client changes the created resource state to "MANTENIMIENTO"
    Then the resource response status must be 200
    And the resource field "estado" must be "MANTENIMIENTO"

  Scenario: Change resource state with invalid value is rejected
    When a client creates the resource "TEST-EstadoMal" of type "OTRO" with capacity 1
    And a client changes the created resource state to "ROTO"
    Then the resource response status must be 400
    And the resource error code must be "VALIDACION"

  Scenario: Create resource without capacity is rejected
    When a client creates a resource without capacity
    Then the resource response status must be 400
    And the resource error code must be "VALIDACION"

  Scenario: Resource in maintenance is not available
    When a client checks availability of resource 2 from "2026-10-20T09:00:00" to "2026-10-20T11:00:00"
    Then the resource response status must be 200
    And the availability must be "false"

  Scenario: Resource with overlapping reservation is not available
    When a client checks availability of resource 1 from "2026-10-20T10:00:00" to "2026-10-20T12:00:00"
    Then the resource response status must be 200
    And the availability must be "false"
    And the overlapping list must contain 1 reservations

  Scenario: Resource with free range is available
    When a client checks availability of resource 1 from "2026-10-20T12:00:00" to "2026-10-20T14:00:00"
    Then the resource response status must be 200
    And the availability must be "true"

  Scenario: Availability of unknown resource returns not found
    When a client checks availability of resource 9999 from "2026-10-20T09:00:00" to "2026-10-20T11:00:00"
    Then the resource response status must be 404
    And the resource error code must be "NO_ENCONTRADO"
