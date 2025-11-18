package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.Selection;

import java.util.Arrays;
import java.util.List;

public class JpaCompoundSelection<X> extends JpaSelection<X> implements CompoundSelection<X> {

    private final List<Selection<?>> selections;

    public JpaCompoundSelection(Class<X> cls, Selection<?>... selections) {
        super(cls);
        this.selections = Arrays.asList(selections);
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        return List.copyOf(selections);
    }

    @Override
    public boolean isCompoundSelection() {
        return true;
    }
}
