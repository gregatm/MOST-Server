package de.muenchen.jpa.dao;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class BookAttributes {
    private int yearOfPublishing;
    private BookCategory category;
}
