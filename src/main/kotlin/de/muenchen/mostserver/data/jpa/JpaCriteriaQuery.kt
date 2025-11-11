package de.muenchen.mostserver.data.jpa

import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Order
import jakarta.persistence.criteria.ParameterExpression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import jakarta.persistence.criteria.Selection
import jakarta.persistence.criteria.Subquery
import jakarta.persistence.metamodel.EntityType

class JpaCriteriaQuery<T>(val clazz: Class<T>): CriteriaQuery<T> {


    override fun select(p0: Selection<out T?>?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun multiselect(vararg p0: Selection<*>?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun multiselect(p0: List<Selection<*>?>?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun where(p0: Expression<Boolean?>?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun where(vararg p0: Predicate?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun where(p0: List<Predicate?>?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun groupBy(vararg p0: Expression<*>?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun groupBy(p0: List<Expression<*>?>?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun having(p0: Expression<Boolean?>?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun having(vararg p0: Predicate?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun having(p0: List<Predicate?>?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun orderBy(vararg p0: Order?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun orderBy(p0: List<Order?>?): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun distinct(p0: Boolean): CriteriaQuery<T?>? {
        TODO("Not yet implemented")
    }

    override fun getOrderList(): List<Order?>? {
        TODO("Not yet implemented")
    }

    override fun <X : Any?> from(p0: Class<X?>?): Root<X?>? {
        TODO("Not yet implemented")
    }

    override fun <X : Any?> from(p0: EntityType<X?>?): Root<X?>? {
        TODO("Not yet implemented")
    }

    override fun getRoots(): Set<Root<*>?>? {
        TODO("Not yet implemented")
    }

    override fun getSelection(): Selection<T?>? {
        TODO("Not yet implemented")
    }

    override fun getGroupList(): List<Expression<*>?>? {
        TODO("Not yet implemented")
    }

    override fun getGroupRestriction(): Predicate? {
        TODO("Not yet implemented")
    }

    override fun isDistinct(): Boolean {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun getParameters(): Set<ParameterExpression<*>?>? {
        TODO("Not yet implemented")
    }
}