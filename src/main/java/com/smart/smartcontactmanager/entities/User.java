package com.smart.smartcontactmanager.entities;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotBlank(message = "Name field should not be empty!!!")
    @Size(min = 2,max = 20,message = "min 2 and max 20 characters required !!!")
    private String name;

    @Column(unique = true)
    private String email;
    private String password;
    private String role;
    private boolean enabled;
    private String imageUrl;

    @Column(length = 500)
    private String about;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user",orphanRemoval = true)
    private List<Contact> contacts = new ArrayList<>();

    public User(){
        super();
    }




}
