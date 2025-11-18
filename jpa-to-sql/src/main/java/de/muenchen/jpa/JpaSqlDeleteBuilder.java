package de.muenchen.jpa;

import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.CriteriaDelete;

import java.util.List;

public class JpaSqlDeleteBuilder {

    public static <X> void build(CriteriaDelete<X> query, X argument) {

    }

    public static <X> void build(StringBuilder sb, CriteriaDelete<X> query, List<Parameter<?>> params, X arg) {

    }
}
