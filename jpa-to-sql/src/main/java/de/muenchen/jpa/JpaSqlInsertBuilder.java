package de.muenchen.jpa;

import jakarta.persistence.Parameter;
import jakarta.persistence.criteria.CriteriaUpdate;

import java.util.List;

public class JpaSqlInsertBuilder {
    public static <X> void build(CriteriaUpdate<X> query) {

    }

    public static <X> void build(StringBuilder sb, CriteriaUpdate<X> query, List<Parameter<?>> params) {

    }
}
