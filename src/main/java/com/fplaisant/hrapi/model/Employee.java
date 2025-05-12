package com.fplaisant.hrapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le prénom ne doit pas être vide")
    @Column(name = "first_name")
    private String firstName;

    @NotBlank(message = "Le nom ne doit pas être vide")
    @Column(name = "last_name")
    private String lastName;

    @Email(message = "Email invalide")
    private String mail;

    @NotBlank(message = "Le mot de passe ne doit pas être vide")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

}
