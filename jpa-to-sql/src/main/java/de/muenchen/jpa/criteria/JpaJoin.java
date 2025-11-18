package de.muenchen.jpa.criteria;

import de.muenchen.jpa.metamodel.EntityTypeImpl;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.SingularAttribute;

public class JpaJoin<Z, X> extends JpaFrom<Z, X> implements Join<Z, X> {

    private final JoinType joinType;

    private Predicate onPredicate;

    private ManagedType<Z> root;

    public JpaJoin(From<?, Z> parent, EntityType<Z> root, EntityType<X> type, JoinType joinType, AbstractJpaExpressionFactory factory) {
        super(parent, type, factory);
        this.joinType = joinType;
        this.root = root;
    }


    @Override
    public Join<Z, X> on(Expression<Boolean> expression) {
        onPredicate = new JpaPredicate(factory, expression);
        return this;
    }

    @Override
    public Join<Z, X> on(Predicate... predicates) {
        onPredicate = factory.and(new JpaPredicate(factory, predicates));
        return this;
    }

    @Override
    public Predicate getOn() {
        if (onPredicate != null) {
            return onPredicate;
        }
        var right = (EntityTypeImpl<X>) this.getType();
        var left = (EntityTypeImpl<Z>) this.root;
        var rightAttr = right.getJoinAttribute(left);
        var leftAttr = left.getJoinAttribute(right);
        var leftPath = switch (leftAttr.getPersistentAttributeType()) {
            case MANY_TO_MANY, MANY_TO_ONE -> this.getParent().get(leftAttr.getName());
            case ONE_TO_MANY -> this.getParent().get(left.getId(left.getIdType().getJavaType()));
            default -> throw new IllegalStateException();
        };
        var rightPath = switch (rightAttr.getPersistentAttributeType()) {
            case MANY_TO_MANY, MANY_TO_ONE -> this.get(rightAttr.getName());
            case ONE_TO_MANY -> this.get(right.getId(right.getIdType().getJavaType()));
            default -> throw new IllegalStateException();
        };
        var leftId = false;
        var rightId = false;
        if (leftPath.getModel() instanceof SingularAttribute<?,?> s) {
            leftId = s.isId();
        }
        if (rightPath.getModel() instanceof SingularAttribute<?,?> s) {
            rightId = s.isId();
        }

        if (leftId && rightId) {
            throw new IllegalStateException("JoinTable not yet implemented");
        }

        return factory.equal(leftPath, rightPath);
    }

    @Override
    public Attribute<? super Z, ?> getAttribute() {
        return null;
    }

    @Override
    public From<?, Z> getParent() {
        return (From<?, Z>) getParentPath();
    }

    @Override
    public JoinType getJoinType() {
        return joinType;
    }

    public void toSql(AliasContext context, StringBuilder sb) {

    }
}
