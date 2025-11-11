package de.muenchen.mostserver.data.jpa.criteria;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;
import org.apache.openjpa.persistence.meta.Types;

public class JpaRoot<X> extends JpaFrom<X, X> implements Root<X> {

    public JpaRoot(Types.Entity<X> type) {
        super(type);

    }

    public JpaRoot(Types.Entity<X> type, JpaExpressionFactory factory) {
        super(type, factory);
    }

    @Override
    public EntityType<X> getModel() {
        return null;
    }
}
