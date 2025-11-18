package de.muenchen.jpa.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
public class Author {

    @Id
    private UUID id;
    private String name;

    @OneToMany
    private List<Book> books;

}
