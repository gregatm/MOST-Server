package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;

public class JpaRoot<X> extends JpaFrom<X, X> implements Root<X> {

    public JpaRoot(EntityType<X> type, AbstractJpaExpressionFactory factory) {
        super(type, factory);
    }

    @Override
    public EntityType<X> getModel() {
        return (EntityType<X>) this.getType();
    }
}
