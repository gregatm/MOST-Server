package de.muenchen.mostserver.data

import de.muenchen.mostserver.odata.EdmEntityAsType
import de.muenchen.mostserver.odata.getIdFieldName
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.sql.Join
import java.lang.reflect.Field
import java.util.Optional
import java.util.function.Supplier

class R2DbcQueryBuilder<T>: QueryBuilder<T> {

    constructor(clazz: Class<T>): super(clazz) {
        tableRef = QueryBuilderTableRef(this)
        from = TableRef(clazz)
    }

    constructor(builder: QueryBuilder<T>): super(builder.clazz) {
        tableRef = QueryBuilderTableRef(this)
        from = builder.tableRef
    }

    override val tableRef: TableRef<T>
    val selects = ArrayList<SqlField>()

    var from: TableRef<*>

    val joins = ArrayList<Triple<Join.JoinType, TableRef<*>, Predicate>>()

    var limit = Optional.empty<Int>()
    var skip = Optional.empty<Int>()

    var wheres = Optional.empty<Predicate>()

    var orderBys = Optional.empty<List<SqlField>>()

    var groupBys = Optional.empty<List<SqlField>>()

    override fun limit(l: Int): QueryBuilder<T> {
       limit = Optional.of(l)
        return this
    }

    override fun skip(s: Int): QueryBuilder<T> {
        skip = Optional.of(s)
        return this
    }

    fun fillTableRef(s: Iterable<SqlField>, tableRef: TableRef<T>) {
        s.filter{ it.tableRef == null}
            .onEach { it.tableRef = tableRef }
    }

    override fun select(vararg s: SqlField): QueryBuilder<T> {
        return select(s.asIterable())
    }

    override fun select(s: Iterable<SqlField>): QueryBuilder<T> {
        val unknownField = s.map { it.clone() }
            .filter { it !is SqlAnySelect }
            .filter { it !is AggregateFunction }
            .onEach { if (it.tableRef == null) it.tableRef = this.from }
            .any { !this.hasColumn(it) }
        if (unknownField) {
            throw RuntimeException("Unknown field")
        }
        fillTableRef(s, tableRef)
        selects.addAll(s)
        return this
    }

    override fun count(b: Boolean): QueryBuilder<T> {
        return this
    }

    override fun groupBy(vararg s: SqlField): QueryBuilder<T> {
        return this
    }

    override fun where(exp: Predicate): QueryBuilder<T> {
        wheres = Optional.of(exp)
        return this
    }

    fun isRelationshipAnnotation(a: Annotation): Boolean {
        return a is ManyToMany || a is ManyToOne || a is OneToMany || a is OneToOne
    }

    fun <T, U> getJoinField(clazzA: Class<T>, clazzB: Class<U>): Field? {
        return clazzA.declaredFields.filter {f -> f.annotations.any(this::isRelationshipAnnotation)}
            .firstOrNull { f ->
                f.type.isAssignableFrom(clazzB) ||
                        (f.annotations.firstOrNull { a -> a is EdmEntityAsType } as EdmEntityAsType?)
                            ?.value?.java?.isAssignableFrom(clazzB) ?: false
            }
    }

    fun checkIsJoinable(clazzA: Class<*>, clazzB: Class<*>): Pair<Field?, Field?> {
        val localF = getJoinField(clazzA, clazzB)
        val joinF = getJoinField(clazzB, clazzA)

        if (localF == null && joinF == null) {
            throw RuntimeException("Class $clazzA cannot join with $clazzB")
        }

        return Pair(localF, joinF)
    }

    fun findJoinPredicate(field: Field?, fieldTableRef: TableRef<*>, clazz: Class<*>, clazzTableRef: TableRef<*>): EqPredicate? {
        val a = field?.getAnnotation(JoinColumn::class.java)
        val name = if (a != null) { getIdFieldName(clazz).firstOrNull() } else return null
        if (name == null) return null
        return EqPredicate(SqlField(fieldTableRef, a.name.ifEmpty {field.name }), SqlField(clazzTableRef, name))
    }

    override fun <U> join(joinTable: TableRef<U>, kind: Join.JoinType): QueryBuilder<T> {
        val joinClazz = joinTable.clazz
        val fields = checkIsJoinable(clazz, joinClazz)
        val jId = joinClazz.fields
            .firstOrNull() { it.annotations.any { a ->  a is Id } }

        val predicate = findJoinPredicate(fields.first, from, joinClazz, joinTable)
            ?: findJoinPredicate(fields.second,joinTable, clazz,from)
        if (predicate == null) {
            throw RuntimeException("Cannot join class $clazz with $joinClazz")
        }

        return join(joinTable, kind, predicate)
    }

    override fun <U> join(joinTable: TableRef<U>, kind: Join.JoinType, predicate: Predicate): QueryBuilder<T> {
        joins.add(Triple(kind,joinTable, predicate))
        return this
    }

    override fun <U> join(subQuery: QueryBuilder<U>, kind: Join.JoinType): QueryBuilder<T> {
        return join(subQuery.tableRef, kind)
    }

    override fun hasColumn(field: SqlField): Boolean {
        if ((field.tableRef == null || field.tableRef == tableRef) && tableRef !is QueryBuilderTableRef) {
            try {
                clazz.getField(field.f)
                return true
            } catch (e: NoSuchFieldException) {}
        }
        if (field.tableRef == null) {
            return false
        }
        return joins.map{ it.second }.plus(from).any{ it.hasField(field) }
    }

    override fun permissions(): QueryBuilder<T> {
        return this
    }

    override fun getFields(): List<SqlField> {
        return listOf(*selects.toTypedArray())
    }

    fun getFieldsForSelectAndGroup(): List<SqlField> {
        return joins
            .map{ it.second }
            .flatMap { it.getFields() }
            .plus(from.getFields())
    }

    override fun fillRefsTableId(p: Supplier<String>) {
        tableRef.tableId = p.get()
        joins
            .map { it.second }
            .plus(from)
            .filter { it is QueryBuilderTableRef }
            .map { it as QueryBuilderTableRef }
            .onEach { it.queryBuilder.fillRefsTableId(p) }
        joins
            .map { it.second }
            .plus(from)
            .filter { it !is QueryBuilderTableRef }
            .onEach { it.tableId = p.get() }
    }

    fun getJoinExp(left: TableRef<*>, right: TableRef<*>): EqPredicate {
        val rf = when (right) {
            is QueryBuilderTableRef<*> -> "id"
            else -> "id"
        }

        val lf = when (left) {
            is QueryBuilderTableRef<*> -> "id"
            else -> "id"
        }

        val l = SqlField(left, lf)
        val r = SqlField(right, rf)

        return EqPredicate(l, r)
    }

    override fun build(): Pair<String, List<*>> {
        fillRefsTableId(TableIdGenerator())
        return super.build()
    }

    override fun toSql(sb: MutableList<Any?>): List<*> {
        val params = ArrayList<Any?>()
        sb.add("SELECT ")
        if (selects.isEmpty()) {
            sb.add("*")
        } else {
            params.addAll(selects[0].toSql(sb))
            for (select in selects.drop(1)) {
                sb.add(", ")
                params.addAll(select.toSql(sb))
            }
        }
        sb.add(" FROM ")

        params.addAll(from.toSql(sb))

        joins
            .onEach { p ->
                sb.add(" ")
                sb.add(p.first.sql)
                sb.add(" ")
                params.addAll(p.second.toSql(sb))
                sb.add(" ON ")
                params.addAll(p.third.toSql(sb))
            }

        wheres.ifPresent {
            sb.add(" WHERE ")
            params.addAll(it.toSql(sb))
        }

        groupBys.ifPresent {
            sb.add(" GROUP BY ")
            params.addAll(it[0].toSql(sb))
            it.drop(1).forEach { f ->
                sb.add(", ")
                params.addAll(f.toSql(sb))
            }

        }

        orderBys.ifPresent {
            sb.add(" ORDER BY ")
            params.addAll(it[0].toSql(sb))
            it.drop(1).forEach { f ->
                sb.add(", ")
                params.addAll(f.toSql(sb))
            }
        }

        limit.ifPresent {
            sb.add(" LIMIT ")
            sb.add(it.toString())
        }

        skip.ifPresent {
            sb.add(" OFFSET ")
            sb.add(it.toString())
        }

        return params
    }
}