Feature: User management

  Background:
    Given the API has registered users

  Scenario: Create user
    When a client creates the user "Test Uno" with document "TEST-001" and email "test1@example.com"
    Then the response status must be 201
    And the created user state must be "ACTIVO"

  Scenario: Get user by id
    When a client creates the user "Test Dos" with document "TEST-002" and email "test2@example.com"
    And a client requests the created user by id
    Then the response status must be 200
    And the response field "nombre" must be "Test Dos"

  Scenario: Change user state
    When a client creates the user "Test Tres" with document "TEST-003" and email "test3@example.com"
    And a client changes the created user state to "INACTIVO"
    Then the response status must be 200
    When a client requests the created user by id
    Then the created user state must be "INACTIVO"

  Scenario: Duplicate email is rejected
    When a client creates the user "Duplicado" with document "TEST-DUP" and email "ana@example.com"
    Then the response status must be 409
    And the error code must be "DUPLICADO"

  Scenario: Unknown user id returns not found
    When a client requests the user with id 9999
    Then the response status must be 404
    And the error code must be "NO_ENCONTRADO"

  Scenario: Create user without name is rejected
    When a client creates a user without name
    Then the response status must be 400
    And the error code must be "VALIDACION"
