package com.fplaisant.hrapi.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmployeeProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public EmployeeProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEmployeeEvent(String topic, String employeeJson) {
        kafkaTemplate.send(topic, employeeJson);
    }
}