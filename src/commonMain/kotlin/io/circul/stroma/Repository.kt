package io.circul.stroma

interface CQRSRepository<ID, T : AggregateRoot<ID, *>> {
    fun find(id: ID): T?
    fun save(aggregate: T)
}

interface CQRSQueryService<Q, T> {
    fun find(query: Q): T?
}

interface CqrsProjectionService {

}

interface CrudRepository<ID, T : AggregateRoot<ID, *>> {
    fun findById(id: ID): T?
    fun save(entity: T)
    fun delete(id: ID)
    fun findAll(): List<T>
}
