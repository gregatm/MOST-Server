package de.muenchen.jpa.criteria;

import jakarta.persistence.Parameter;

import java.util.List;

public interface JpaSql {
    void toSql(StringBuilder sb, AliasContext context, List<Parameter<?>> params);
}
