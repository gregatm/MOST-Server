package de.muenchen.mostserver.odata

import de.muenchen.mostserver.data.AndPredicate
import de.muenchen.mostserver.data.EqPredicate
import de.muenchen.mostserver.data.LikePredicate
import de.muenchen.mostserver.data.Predicate
import de.muenchen.mostserver.data.QueryBuilder
import de.muenchen.mostserver.data.SqlAnySelect
import de.muenchen.mostserver.data.SqlField
import de.muenchen.mostserver.data.SqlLiteral
import de.muenchen.mostserver.data.SqlUnsafeLiteral
import de.muenchen.mostserver.data.SqlValue
import org.apache.olingo.server.api.uri.UriInfoResource
import org.apache.olingo.server.api.uri.UriParameter
import org.apache.olingo.server.api.uri.UriResource
import org.apache.olingo.server.api.uri.UriResourceEntitySet
import org.apache.olingo.server.api.uri.UriResourceKind
import org.apache.olingo.server.api.uri.UriResourceNavigation
import org.apache.olingo.server.api.uri.queryoption.FilterOption
import org.apache.olingo.server.api.uri.queryoption.expression.Binary
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind
import org.apache.olingo.server.api.uri.queryoption.expression.Expression
import org.apache.olingo.server.api.uri.queryoption.expression.Literal
import org.apache.olingo.server.api.uri.queryoption.expression.Member
import org.apache.olingo.server.api.uri.queryoption.expression.Method
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind
import org.springframework.data.relational.core.sql.Like
import org.springframework.data.relational.core.sql.StatementBuilder
import org.springframework.data.relational.core.sql.Table
import java.util.Optional
import java.util.function.Function

fun valueToSqlValue(exp: Any?, transform: (String) -> String = { t -> t}): SqlValue {
    return when (exp) {
        is Literal -> SqlUnsafeLiteral(transform(exp.text))
        is Member -> SqlField(null, exp.resourcePath.uriResourceParts.last().segmentValue)
        else -> throw RuntimeException("No value found")
    }
}

fun operatorKindToPredicate(kind: BinaryOperatorKind, left: Expression, right: Expression): Predicate {
    return when(kind) {
        BinaryOperatorKind.AND -> {AndPredicate(expressionToPredicate(left), expressionToPredicate(right))
        }
        BinaryOperatorKind.EQ -> EqPredicate(valueToSqlValue(left), valueToSqlValue(right))
        else -> throw RuntimeException("No operator found")
    }
}

fun methodKindToPredicate(kind: MethodKind, parameters: List<*>): Predicate {
    return when(kind) {
        MethodKind.CONTAINS -> LikePredicate(valueToSqlValue(parameters[0]) as SqlField, valueToSqlValue(parameters[1], {t -> "%${t.substring(1).substringBeforeLast("'")}%"}) as SqlLiteral<String>)
        MethodKind.STARTSWITH -> LikePredicate(valueToSqlValue(parameters[0]) as SqlField, valueToSqlValue(parameters[1], {t -> "${t.substring(1).substringBeforeLast("'")}%"}) as SqlLiteral<String>)
        else -> throw RuntimeException("No method found")
    }
}

fun expressionToPredicate(exp: Expression): Predicate {
    return when (exp) {
        is Binary -> operatorKindToPredicate(exp.operator, exp.leftOperand, exp.rightOperand)
        is Method -> methodKindToPredicate(exp.method, exp.parameters)
        else -> throw RuntimeException("No expression found")
    }
}

fun filterOptionToPredicate(filter: FilterOption): Predicate {
    return expressionToPredicate(filter.expression)
}

fun urlPathTo(res: UriResource, info: Optional<UriInfoResource>, provider: EdmEntityProviderGenerated): QueryBuilder<*> {
    return when (res.kind) {
        UriResourceKind.entitySet -> {
            val set = res as UriResourceEntitySet
            val entity = provider.getEntityType(set.entityType.fullQualifiedName)
            val idWhere = set.keyPredicates.map<UriParameter, Predicate> {
                EqPredicate(SqlField(null, it.name), SqlUnsafeLiteral(it.text))
            }
                .reduce { l,r -> AndPredicate(l,r) }
            QueryBuilder.from(entity!!.typeClass)
                .where(idWhere).select(SqlField(null, "id"))
        }
        UriResourceKind.navigationProperty -> {
            val nav = res as UriResourceNavigation
            val entity = provider.getEntityType(nav.type.fullQualifiedName)

            val builder = QueryBuilder.from(entity!!.typeClass)
                info.ifPresent { info ->
                    if (info.topOption != null) {
                        builder.limit(info.topOption.value)
                    }

                    if (info.skipOption != null) {
                        builder.skip(info.skipOption.value)

                    }

                    if (info.selectOption != null) {
                        builder.select(
                            info.selectOption.selectItems
                                .map { item -> item.resourcePath.uriResourceParts.last().segmentValue }
                                .map { SqlField(null, it) }
                                .toList()
                        )
                    } else {
                        builder.select(SqlAnySelect(null))
                    }

                    if (info.expandOption != null) {

                    }

                    if (info.filterOption != null) {
                        val predicate = expressionToPredicate(info.filterOption.expression)
                        builder.where(predicate)
                    }
                }


            builder
        }
        else -> throw RuntimeException("Unknown resource kind")
    }
}