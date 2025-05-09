package com.fplaisant.hrapi;

import com.fplaisant.hrapi.model.Employee;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidEmployee() {
        Employee employee = new Employee();
        employee.setFirstName("Jean");
        employee.setLastName("Dupont");
        employee.setMail("jean.dupont@email.com");
        employee.setPassword("SecurePass123");

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "L'objet Employee devrait être valide");
    }

    @Test
    void testInvalidEmail() {
        Employee employee = new Employee();
        employee.setFirstName("Jean");
        employee.setLastName("Dupont");
        employee.setMail("invalid-email");  // Email incorrect
        employee.setPassword("SecurePass123");

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "L'email est invalide et devrait générer une erreur");
    }

    @Test
    void testShortPassword() {
        Employee employee = new Employee();
        employee.setFirstName("Jean");
        employee.setLastName("Dupont");
        employee.setMail("jean.dupont@email.com");
        employee.setPassword("short");  // Mot de passe trop court

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "Le mot de passe est trop court et devrait générer une erreur");
    }
}