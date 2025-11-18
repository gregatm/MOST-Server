package de.muenchen.jpa.dao;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Book {

    @Id
    private UUID id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    @Embedded
    private BookAttributes attributes;
}
