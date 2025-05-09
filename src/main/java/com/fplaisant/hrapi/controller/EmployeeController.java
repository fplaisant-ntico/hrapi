package com.fplaisant.hrapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fplaisant.hrapi.model.Employee;
import com.fplaisant.hrapi.service.EmployeeProducer;
import com.fplaisant.hrapi.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeProducer employeeProducer;

    private ObjectMapper objectMapper = new ObjectMapper();




    @GetMapping("/")
    public String getResource() {
        return "a value...";
    }


    @GetMapping("/employees")
    public Iterable<Employee> getEmployees() {
        return employeeService.getEmployees();
    }

    @GetMapping("/employee/{id}")
    public Employee getEmployee(@PathVariable("id") final Long id) {
        Optional<Employee> employee = employeeService.getEmployee(id);

        return employee.orElse(null);
    }

    @DeleteMapping("/employee/{id}")
    public void deleteEmployee(@PathVariable("id") final Long id) {
        employeeService.deleteEmployee(id);
        String listEmployee = null;
        try {
            listEmployee = objectMapper.writeValueAsString(employeeService.getEmployees());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        employeeProducer.sendEmployeeEvent("employee-delete", listEmployee);
    }

    @PostMapping("/employee")
    public ResponseEntity<?> addEmployee(@Valid @RequestBody Employee employee, BindingResult result) {
        if (result.hasErrors()) {
            // Construire une liste des messages d'erreur
            List<String> errors = result.getFieldErrors().stream()
                    .map(error -> error.getField() + " : " + error.getDefaultMessage())
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest().body(errors); // Retourne la liste des erreurs en JSON
        }

        Employee savedEmployee = employeeService.saveEmployee(employee);
        String listEmployee = null;
        try {
            listEmployee = objectMapper.writeValueAsString(employeeService.getEmployees());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        employeeProducer.sendEmployeeEvent("employee-add", listEmployee);
        return ResponseEntity.ok(savedEmployee);
    }


    @PutMapping("/employee/{id}")
    public Employee updateEmployee(@PathVariable("id") final Long id, @Valid @RequestBody Employee newEmployee) {
        Optional<Employee> employee = employeeService.getEmployee(id);
        if(employee.isPresent()) {
            Employee currentEmployee = employee.get();
            if(newEmployee.getFirstName() != null) currentEmployee.setFirstName(newEmployee.getFirstName());
            if(newEmployee.getLastName() != null) currentEmployee.setLastName(newEmployee.getLastName());
            if(newEmployee.getMail() != null) currentEmployee.setMail(newEmployee.getMail());
            if(newEmployee.getPassword() != null) currentEmployee.setPassword(newEmployee.getPassword());
            employeeService.saveEmployee(currentEmployee);
            String listEmployee = null;
            try {
                listEmployee = objectMapper.writeValueAsString(employeeService.getEmployees());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            employeeProducer.sendEmployeeEvent("employee-update", listEmployee);
            return currentEmployee;
        } else {
            return null;
        }

    }

}
