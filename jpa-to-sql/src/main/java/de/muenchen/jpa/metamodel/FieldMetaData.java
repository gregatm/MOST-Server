package de.muenchen.jpa.metamodel;

import jakarta.persistence.metamodel.Attribute;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.Optional;

@Getter
@Setter
public class FieldMetaData<X> {
    private final Field field;
    private boolean isVersion = false;
    private boolean isId = false;
    private String sqlName;
    private boolean isEmbeddable = false;
    private boolean isTransient = false;
    private boolean isOptional = true;
    private Attribute.PersistentAttributeType attributeType = Attribute.PersistentAttributeType.BASIC;

    public FieldMetaData(Class<X> cls, Field field) {
        this.field = field;
    }

    public String getSqlIdentifier() {
        return Optional.ofNullable(sqlName)
                .orElseGet(field::getName);
    }
}
