package de.muenchen.mostserver.data.jpa

import de.muenchen.mostserver.data.jpa.criteria.AbstractJpaExpressionFactory
import de.muenchen.mostserver.data.jpa.criteria.JpaPredicate
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Order
import jakarta.persistence.criteria.ParameterExpression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import jakarta.persistence.criteria.Selection
import jakarta.persistence.criteria.Subquery
import jakarta.persistence.metamodel.EntityType

class JpaCriteriaQuery<T>(val clazz: Class<T>, val factory: AbstractJpaExpressionFactory): CriteriaQuery<T> {

    private var wheres: Predicate? = null
    private var selects: Selection<T?>? = null
    private val groupBys: MutableSet<Expression<*>> = HashSet()
    private var havings: Predicate? = null
    private val orderBys: MutableList<Order> = ArrayList()
    private val roots: MutableSet<Root<*>> = HashSet()
    private var isDistinct: Boolean = false

    var limit: Integer? = null;
    var offset: Integer? = null;

    fun limit(limit: Integer?): CriteriaQuery<T>? {
        this.limit = limit
        return this
    }

    fun offset(offset: Integer?): CriteriaQuery<T>? {
        this.offset = offset
        return this
    }

    override fun select(exp: Selection<out T?>?): CriteriaQuery<T>? {
        selects = exp as Selection<T?>
        return this
    }

    @Deprecated("Deprecated in Java")
    override fun multiselect(vararg exp: Selection<*>?): JpaCriteriaQuery<T>? {
        select(factory.construct(this.clazz, *exp.filterNotNull().toTypedArray()))
        return this
    }

    @Deprecated("Deprecated in Java")
    override fun multiselect(selects: List<Selection<*>?>?): CriteriaQuery<T>? {
        if (selects != null) {
            multiselect(*selects.toTypedArray())
        }
        return this
    }

    override fun where(exp: Expression<Boolean?>?): CriteriaQuery<T>? {
        if (exp != null) {
            return where(JpaPredicate(factory, exp))
        }
        this.wheres = null
        return this
    }

    override fun where(vararg exp: Predicate?): CriteriaQuery<T> {
        if (exp.isEmpty()) {
            this.wheres = null;
        }
        this.wheres = factory.and(exp.filterNotNull())
        return this
    }

    override fun where(exp: List<Predicate?>?): CriteriaQuery<T>? {
        if (exp != null) {
            return where(*exp.toTypedArray())
        }
        this.wheres = null
        return this
    }

    override fun groupBy(vararg exp: Expression<*>?): CriteriaQuery<T>? {
        exp.filterNotNull()
            .forEach(groupBys::add)
        return this
    }

    override fun groupBy(exp: List<Expression<*>?>?): CriteriaQuery<T>? {
        if (exp != null) {
            return groupBy(*exp.toTypedArray())
        }
        return this
    }

    override fun having(exp: Expression<Boolean?>?): CriteriaQuery<T>? {
        if (exp != null) {
            return having(JpaPredicate(factory, exp))
        }
        this.havings = null;
        return this
    }

    override fun having(vararg exp: Predicate?): CriteriaQuery<T>? {
        if (exp.isEmpty()) {
            this.havings = null;
        }
        this.havings = factory.and(exp.filterNotNull())
        return this
    }

    override fun having(exp: List<Predicate?>?): CriteriaQuery<T>? {
        if (exp != null) {
            return having(*exp.toTypedArray())
        }
        this.havings = null;
        return this
    }

    override fun orderBy(vararg exp: Order?): CriteriaQuery<T>? {
        exp.filterNotNull()
            .forEach(orderBys::add)
        return this
    }

    override fun orderBy(exp: List<Order?>?): CriteriaQuery<T>? {
        if (exp != null) {
            return orderBy(*exp.toTypedArray())
        }
        return this
    }

    override fun distinct(d: Boolean): CriteriaQuery<T>? {
        this.isDistinct = d
        return this
    }

    override fun getOrderList(): List<Order?>? {
        return orderBys
    }

    override fun <X : Any?> from(cls: Class<X?>?): Root<X?>? {
        if (cls != null) {
            val root = factory.from(cls)
            roots.add(root)
            return root
        }
        TODO("Not yet implemented")
    }

    override fun <X : Any?> from(et: EntityType<X?>?): Root<X?>? {
        if (et != null) {
            val root = factory.from(et)
            roots.add(root)
            return root
        }
        TODO("Not yet implemented")
    }

    fun <X : Any?> from(query: CriteriaQuery<X>?): Root<X?>? {
        TODO("Not yet implemented")
    }

    override fun getRoots(): Set<Root<*>?>? {
        return roots
    }

    override fun getSelection(): Selection<T?>? {
        return selects
    }

    override fun getGroupList(): List<Expression<*>?>? {
        return groupBys.toList()
    }

    override fun getGroupRestriction(): Predicate? {
        return havings
    }

    override fun isDistinct(): Boolean {
        return this.isDistinct
    }

    override fun getResultType(): Class<T>? {
        return clazz
    }

    override fun <U : Any?> subquery(p0: Class<U?>?): Subquery<U?>? {
        TODO("Not yet implemented")
    }

    override fun <U : Any?> subquery(p0: EntityType<U?>?): Subquery<U?>? {
        TODO("Not yet implemented")
    }

    override fun getRestriction(): Predicate? {
        return wheres
    }

    override fun getParameters(): Set<ParameterExpression<*>?>? {
        TODO("Not yet implemented")
    }

    fun toSql(): String {
        return ""
    }
}