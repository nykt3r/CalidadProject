Feature: User listing

  Background:
    Given the API has registered users

  Scenario: List registered users
    When a client requests the list of users
    Then the response must be successful
    And the list must contain 2 users
    And the user "Ana García" must be "ACTIVO"
    And the user "Luis Pérez" must be "INACTIVO"