package de.muenchen.jpa.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    private Integer id;

    @ManyToOne
    private Book book;

    @Version
    private Short version;
}
