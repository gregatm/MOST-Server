package de.muenchen.jpa.metamodel;

import jakarta.persistence.metamodel.Type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
public class EntityMetaData<X> {
    private String table;
    private final Class<X> clazz;
    private final Set<FieldMetaData<X>> fields = new HashSet<>();
    private Type.PersistenceType persistenceType;

    public void addField(FieldMetaData<X> f) {
        this.fields.add(f);
    }

    public String getEntityName() {
        return Optional.ofNullable(table)
                .orElseGet(this.clazz::getSimpleName);
    }
}
