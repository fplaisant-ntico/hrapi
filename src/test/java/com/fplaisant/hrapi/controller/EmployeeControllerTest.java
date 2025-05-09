package com.fplaisant.hrapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fplaisant.hrapi.model.Employee;
import com.fplaisant.hrapi.service.EmployeeProducer;
import com.fplaisant.hrapi.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the EmployeeController class.
 *
 * This test class verifies the behavior of methods within the EmployeeController
 * by mocking dependencies such as EmployeeService, ObjectMapper, and EmployeeProducer.
 *
 * Tests include:
 * - Retrieving all employees.
 * - Retrieving a specific employee by ID when the employee is found or not found.
 * - Adding a new employee with valid or invalid input.
 * - Updating an existing employee when the employee is found or not found.
 * - Deleting an employee.
 *
 * Mocks are used to isolate dependencies and to simulate expected behavior, enabling
 * focused validation of controller logic.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {
    private static final Long EXISTING_ID = 1L;
    private static final String TOPIC_DELETE = "employee-delete";
    private static final String TOPIC_ADD = "employee-add";
    private static final String TOPIC_UPDATE = "employee-update";
    private static final String MOCK_JSON = "mock-employee-list";

    @Mock
    private EmployeeService employeeService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private EmployeeProducer employeeProducer;
    @InjectMocks
    private EmployeeController employeeController;

    private Employee createTestEmployee(String firstName, String lastName) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        return employee;
    }

    private Employee createValidEmployee() {
        Employee employee = createTestEmployee("Jean", "Dupont");
        employee.setMail("jean.dupont@email.com");
        employee.setPassword("SecurePass123");
        return employee;
    }

    @Test
    void shouldReturnAllEmployees() {
        List<Employee> mockEmployees = List.of(
                createTestEmployee("Jean", "Dupont"),
                createTestEmployee("Marie", "Curie")
        );
        when(employeeService.getEmployees()).thenReturn(mockEmployees);

        Iterable<Employee> result = employeeController.getEmployees();

        assertNotNull(result);
        assertEquals(2, ((List<Employee>) result).size());
        verify(employeeService).getEmployees();
    }

    @Test
    void shouldReturnEmployeeWhenExists() {
        Employee mockEmployee = createTestEmployee("Jean", "Dupont");
        mockEmployee.setId(EXISTING_ID);
        when(employeeService.getEmployee(EXISTING_ID)).thenReturn(Optional.of(mockEmployee));

        Employee result = employeeController.getEmployee(EXISTING_ID);

        assertNotNull(result);
        assertEquals(EXISTING_ID, result.getId());
        assertEquals("Jean", result.getFirstName());
        verify(employeeService).getEmployee(EXISTING_ID);
    }

    @Test
    void shouldDeleteEmployeeAndNotifyKafka() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn(MOCK_JSON);

        employeeController.deleteEmployee(EXISTING_ID);

        verify(employeeService).deleteEmployee(EXISTING_ID);
        verify(employeeProducer).sendEmployeeEvent(TOPIC_DELETE, MOCK_JSON);
    }

    @Test
    void shouldSaveValidEmployeeAndReturnSuccess() throws JsonProcessingException {
        Employee newEmployee = createValidEmployee();
        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(newEmployee);
        when(objectMapper.writeValueAsString(any())).thenReturn(MOCK_JSON);

        ResponseEntity<?> response = employeeController.addEmployee(newEmployee, mock(BindingResult.class));

        assertEquals(200, response.getStatusCode().value());
        verify(employeeService).saveEmployee(any(Employee.class));
        verify(employeeProducer).sendEmployeeEvent(TOPIC_ADD, MOCK_JSON);
    }

    @Test
    void shouldReturnErrorForInvalidEmployee() {
        Employee invalidEmployee = new Employee();
        invalidEmployee.setMail("invalid-email");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(new FieldError("employee", "mail", "Email invalide")));

        ResponseEntity<?> response = employeeController.addEmployee(invalidEmployee, bindingResult);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldUpdateExistingEmployeeAndNotifyKafka() throws JsonProcessingException {
        Employee existingEmployee = createTestEmployee("Jean", "Dupont");
        existingEmployee.setId(EXISTING_ID);
        Employee updatedEmployee = createTestEmployee("Paul", "Dupont");

        when(employeeService.getEmployee(EXISTING_ID)).thenReturn(Optional.of(existingEmployee));
        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(updatedEmployee);
        when(objectMapper.writeValueAsString(any())).thenReturn(MOCK_JSON);

        Employee result = employeeController.updateEmployee(EXISTING_ID, updatedEmployee);

        assertNotNull(result);
        assertEquals("Paul", result.getFirstName());
        verify(employeeService).saveEmployee(any(Employee.class));
        verify(employeeProducer).sendEmployeeEvent(TOPIC_UPDATE, MOCK_JSON);
    }
}