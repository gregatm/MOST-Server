package de.muenchen.mostserver.data

import jakarta.persistence.Table
import org.apache.http.MethodNotSupportedException
import org.springframework.data.relational.core.sql.Join
import java.util.LinkedList
import java.util.Optional
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull

abstract class QueryBuilder<T>(val clazz: Class<T>): ToSql {

    abstract val tableRef: TableRef<T>

    companion object {
        fun <T> from(clazz: Class<T>): QueryBuilder<T> {
            return R2DbcQueryBuilder(clazz)
        }

        fun <T> from(from: QueryBuilder<T>): QueryBuilder<T> {
            return R2DbcQueryBuilder(from)
        }
    }

    abstract fun limit(l: Int): QueryBuilder<T>;
    abstract fun skip(s: Int): QueryBuilder<T>;
    abstract fun select(vararg s: SqlField): QueryBuilder<T>
    abstract fun select(s: Iterable<SqlField>): QueryBuilder<T>
    abstract fun count(b: Boolean): QueryBuilder<T>
    fun count(): QueryBuilder<T> {
        this.count(true)
        return this
    }
    abstract fun groupBy(vararg s: SqlField): QueryBuilder<T>
    abstract fun where(exp: Predicate): QueryBuilder<T>

    abstract fun <U> join(joinTable: TableRef<U>, kind: Join.JoinType): QueryBuilder<T>
    abstract fun <U> join(subQuery: QueryBuilder<U>, kind: Join.JoinType): QueryBuilder<T>
    abstract fun <U> join(joinTable: TableRef<U>, kind: Join.JoinType, predicate: Predicate): QueryBuilder<T>

    abstract fun hasColumn(field: SqlField): Boolean
    abstract fun permissions(): QueryBuilder<T>

    abstract fun fillRefsTableId(p: Supplier<String>)

    abstract fun getFields(): List<SqlField>

    open fun build(): Pair<String, List<*>> {
        val ll = LinkedList<Any?>()
        val params = toSql(ll)

        val sb = StringBuilder()
        var count = 1
        for (item in ll) {
            if (item is SqlParameterPlaceholder) {
                sb.append(" $")
                sb.append(count)
                sb.append(" ")
                count += 1
            } else {
                sb.append(item.toString())
            }
        }

        return Pair(sb.toString(), params)
    }
}

abstract class Predicate: ToSql

abstract class LeftRightValuePredicate(val left: SqlValue, val right: SqlValue, val operator: String): Predicate() {
    override fun toSql(sb: MutableList<Any?>): List<*> {
        val params = ArrayList<Any?>()
        params.addAll(left.toSql(sb))
        sb.add(" ")
        sb.add(operator)
        sb.add(" ")
        params.addAll(right.toSql(sb))
        return params
    }
}

open class LeftRightPredicate(val left: Predicate, val right: Predicate, val operator: String): Predicate() {
    override fun toSql(sb: MutableList<Any?>): List<*> {
        val params = ArrayList<Any?>()
        params.addAll(left.toSql(sb))
        sb.add(" ")
        sb.add(operator)
        sb.add(" ")
        params.addAll(right.toSql(sb))
        return params
    }
}

class AndPredicate(left: Predicate, right: Predicate): LeftRightPredicate(left, right, "AND")

class EqPredicate(left: SqlValue, right: SqlValue): LeftRightValuePredicate(left, right, "=")

class LikePredicate(left: SqlField, right: SqlLiteral<String>): LeftRightValuePredicate(left, right, "LIKE")

abstract class SqlValue: ToSql

open class SqlLiteral<T>(val f: T, val asString: Boolean = false) : SqlValue() {
    override fun toSql(sb: MutableList<Any?>): List<*> {
        val quote = if (asString) "'" else ""
        sb.add(quote)
        sb.add(f)
        sb.add(quote)
        return emptyList<Any>()
    }
}

class SqlUnsafeLiteral<T>(f: T): SqlLiteral<T>(f) {
    override fun toSql(sb: MutableList<Any?>): List<*> {
        sb.add(SqlParameterPlaceholder())
        return listOf(f)
    }
}

class SqlParameterPlaceholder

open class SqlField(var tableRef: TableRef<*>?, val f: String) : SqlValue(), Cloneable {

    var alias = Optional.empty<String>()

    open fun setFieldName(name: String?) {
        alias = Optional.ofNullable(name)
    }

    fun getFieldName(): String {
        return alias.orElse(f)
    }

    protected fun toSqlPrependTableRef(sb: MutableList<Any?>) {
        if (tableRef != null && tableRef!!.tableId.isNotEmpty()) {
            sb.add(tableRef!!.tableId)
            sb.add(".")
        }
    }

    override fun toSql(sb: MutableList<Any?>): List<Any> {
        toSqlPrependTableRef(sb)
        sb.add("\"")
        sb.add(f)
        sb.add("\"")
        return emptyList()
    }

    public override fun clone(): SqlField {
        val field = SqlField(tableRef, f)
        field.alias = alias
        return field
    }
}

interface ToSql {

    /**
     *
     * @param sb StringBuilder to append the sql statement to
     * @return literal values for bind to statement
     */
    fun toSql(sb: MutableList<Any?>): List<*>
}

open class AggregateFunction(tableRef: TableRef<*>?, f: String): SqlField(tableRef, f)

class CountAggregateFunction(tableRef: TableRef<*>?, f: String = "*"): AggregateFunction(tableRef, f) {
    override fun toSql(sb: MutableList<Any?>): List<Any> {
        sb.add("COUNT(\"")
        sb.add(f)
        sb.add("\")")
        return emptyList()
    }

    override fun clone(): CountAggregateFunction {
        val field = CountAggregateFunction(tableRef, f)
        field.alias = alias
        return field
    }
}

class SqlAnySelect(tableRef: TableRef<*>?): SqlField(tableRef, "*") {

    override fun setFieldName(name: String?) {
        throw MethodNotSupportedException("Cannot set fieldName on SqlAnySelect")
    }

    override fun clone(): SqlAnySelect {
        return SqlAnySelect(tableRef)
    }

    override fun toSql(sb: MutableList<Any?>): List<Any> {
        sb.add(f)
        return emptyList()
    }
}

open class TableRef<T>(val clazz: Class<T>): ToSql {
    var tableId: String = ""

    fun field(f: String): SqlField {
        hasField(f)
        return SqlField(this, f)
    }

    open fun hasField(f: String): Boolean {
        try {
            // TODO restrict to fields that are exposed as columns
            clazz.getDeclaredField(f)
        } catch (e: NoSuchFieldException) {
            return false;
        }
        return true
    }

    open fun getFields(): List<SqlField> {
        // TODO restrict to fields that are as exposed as columns and map their alias
        return clazz.declaredFields
            .map { f -> SqlField(this, f.name) }
    }

    open fun hasField(f: SqlField): Boolean {
        if (f.tableRef != this) return false
        return hasField(f.f)
    }

    fun getTableFromClass(clazz: Class<*>): String {
        val a = clazz.getAnnotation(Table::class.java)
        return if (a == null || a.name.isEmpty()) {
            clazz.simpleName
        } else {
            a.name
        }
    }

    override fun toSql(sb: MutableList<Any?>): List<*> {
        sb.add(getTableFromClass(clazz))
        sb.add(" as \"")
        sb.add(tableId)
        sb.add("\"")
        return emptyList<Any>()
    }
}

class QueryBuilderTableRef<T>(val queryBuilder: QueryBuilder<T>): TableRef<T>(queryBuilder.clazz) {
    override fun hasField(f: String): Boolean {
        return queryBuilder.hasColumn(SqlField(this, f))
    }

    override fun hasField(f: SqlField): Boolean {
        return queryBuilder.hasColumn(f)
    }

    override fun getFields(): List<SqlField> {
        return queryBuilder.getFields()
    }

    override fun toSql(sb: MutableList<Any?>): List<*> {
        sb.add("(")
        val params = queryBuilder.toSql(sb)
        sb.add(") as \"")
        sb.add(tableId)
        sb.add("\"")
        return params
    }
}

abstract class WhereExpressionBuilder<T>(val queryBuilder: QueryBuilder<T>): Predicate() {

    abstract fun not(exp: Predicate): WhereExpressionBuilder<T>
    abstract fun and(exp: Predicate): WhereExpressionBuilder<T>
    abstract fun or(exp: Predicate): WhereExpressionBuilder<T>
    abstract fun exp(exp: Predicate): WhereExpressionBuilder<T>
    abstract fun end(): QueryBuilder<T>

}

abstract class WhereExpression<T>

class TableIdGenerator: Supplier<String> {
    var state = 0

    override fun get(): String {
        val sb = StringBuilder()
        var next = state++
        do {
            sb.append('a'+(next%26))
            next /= 26
        } while (next > 0)
        return sb.toString()
    }
}
