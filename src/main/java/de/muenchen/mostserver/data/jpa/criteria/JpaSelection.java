package de.muenchen.mostserver.data.jpa.criteria;

import jakarta.persistence.criteria.Selection;
import org.apache.openjpa.persistence.util.ReservedWords;

import java.util.List;

public abstract class JpaSelection<X> implements Selection<X> {

    private final Class<X> cls;
    private String alias;

    public JpaSelection(Class<X> cls) {
        this.cls = cls;
    }

    @Override
    public Selection<X> alias(String s) {
        assertValidName(s);
        this.alias = alias;
        return this;
    }

    @Override
    public boolean isCompoundSelection() {
        return false;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        throw new IllegalStateException(this + " is not a compound selection");
    }

    @Override
    public Class<? extends X> getJavaType() {
        return cls;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    void assertValidName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("empty name is invalid");
        if (ReservedWords.isKeyword(name))
            throw new IllegalArgumentException("reserved word " + name + " is not valid");
        Character ch = ReservedWords.hasSpecialCharacter(name);
        if (ch != null)
            throw new IllegalArgumentException(name + " contains reserved symbol " + ch);
    }

}
