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

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmployeeProducer employeeProducer;

    @InjectMocks
    private EmployeeController employeeController;

    @Test
    void testGetEmployees() {
        // Création de mock employees
        Employee emp1 = new Employee();
        emp1.setFirstName("Jean");
        emp1.setLastName("Dupont");

        Employee emp2 = new Employee();
        emp2.setFirstName("Marie");
        emp2.setLastName("Curie");

        List<Employee> mockEmployees = List.of(emp1, emp2);

        // Simulation du comportement du service
        when(employeeService.getEmployees()).thenReturn(mockEmployees);

        // Appel de la méthode du contrôleur
        Iterable<Employee> employees = employeeController.getEmployees();

        // Vérifications
        assertNotNull(employees);
        assertEquals(2, ((List<Employee>) employees).size());
        verify(employeeService, times(1)).getEmployees();
    }

    @Test
    void testGetEmployeeById_Found() {
        // Création d'un mock employee
        Employee mockEmployee = new Employee();
        mockEmployee.setId(1L);
        mockEmployee.setFirstName("Jean");

        // Simulation du service
        when(employeeService.getEmployee(1L)).thenReturn(java.util.Optional.of(mockEmployee));

        // Appel du contrôleur
        Employee employee = employeeController.getEmployee(1L);

        // Vérifications
        assertNotNull(employee);
        assertEquals(1L, employee.getId());
        assertEquals("Jean", employee.getFirstName());
        verify(employeeService, times(1)).getEmployee(1L);
    }

    @Test
    void testGetEmployeeById_NotFound() {
        // Simulation du service (employé non trouvé)
        when(employeeService.getEmployee(2L)).thenReturn(java.util.Optional.empty());

        // Appel du contrôleur
        Employee employee = employeeController.getEmployee(2L);

        // Vérifications
        assertNull(employee);
        verify(employeeService, times(1)).getEmployee(2L);
    }

    @Test
    void testDeleteEmployee() throws JsonProcessingException {
        Long idToDelete = 1L;

        // Mock du service pour la suppression
        doNothing().when(employeeService).deleteEmployee(idToDelete);
        when(objectMapper.writeValueAsString(any())).thenReturn("mock-employee-list");

        // Appel du contrôleur
        employeeController.deleteEmployee(idToDelete);

        // Vérifications
        verify(employeeService, times(1)).deleteEmployee(idToDelete);
        verify(employeeProducer, times(1)).sendEmployeeEvent(eq("employee-delete"), anyString());
    }

    @Test
    void testAddEmployee_Valid() throws JsonProcessingException {
        Employee newEmployee = new Employee();
        newEmployee.setFirstName("Jean");
        newEmployee.setLastName("Dupont");
        newEmployee.setMail("jean.dupont@email.com");
        newEmployee.setPassword("SecurePass123");

        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(newEmployee);
        when(objectMapper.writeValueAsString(any())).thenReturn("mock-employee-list");

        ResponseEntity<?> response = employeeController.addEmployee(newEmployee, mock(BindingResult.class));

        assertEquals(200, response.getStatusCode().value());
        verify(employeeService, times(1)).saveEmployee(any(Employee.class));
        verify(employeeProducer, times(1)).sendEmployeeEvent(eq("employee-add"), anyString());
    }

    @Test
    void testAddEmployee_Invalid() {
        Employee newEmployee = new Employee();
        newEmployee.setMail("invalid-email"); // Mauvais email

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("employee", "mail", "Email invalide")));

        ResponseEntity<?> response = employeeController.addEmployee(newEmployee, bindingResult);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testUpdateEmployee_Found() throws JsonProcessingException {
        Employee existingEmployee = new Employee();
        existingEmployee.setId(1L);
        existingEmployee.setFirstName("Jean");

        Employee updatedEmployee = new Employee();
        updatedEmployee.setFirstName("Paul"); // Mise à jour

        when(employeeService.getEmployee(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(updatedEmployee);
        when(objectMapper.writeValueAsString(any())).thenReturn("mock-employee-list");

        Employee response = employeeController.updateEmployee(1L, updatedEmployee);

        assertNotNull(response);
        assertEquals("Paul", response.getFirstName());
        verify(employeeService, times(1)).saveEmployee(any(Employee.class));
        verify(employeeProducer, times(1)).sendEmployeeEvent(eq("employee-update"), anyString());
    }

    @Test
    void testUpdateEmployee_NotFound() {
        when(employeeService.getEmployee(2L)).thenReturn(Optional.empty());

        Employee response = employeeController.updateEmployee(2L, new Employee());

        assertNull(response);
    }
}