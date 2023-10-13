package com.smart.smartcontactmanager.repositories;

import com.smart.smartcontactmanager.entities.Contact;
import com.smart.smartcontactmanager.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {


    //pagination
    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId")
    //current-page
    //contact per page
    public Page<Contact>  findContactsByUser(@Param("userId") int userId, Pageable pageable);

    //search
    public List<Contact> findByNameContainingAndUser(String name, User user);


}
