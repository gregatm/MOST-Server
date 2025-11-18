# JPA-to-SQL

Implementing the Jakarta Persistence API for generating SQL statements

## Example

```java
var query = builder.createQuery();
var author = query.from(Author.class);
var book = root.join(Book.class);
q.where(builder.equal(book.get("name"), "Oliver Twist"));
q.order(builder.asc(author.get("name")));

var sql = buildSql(query);
```

generates the query

```sql
SELECT * FROM Author a JOIN Book b ON "author"."id" = "book"."author_id"
    WHERE "b"."name" = 'Oliver Twist'
    ORDER BY "a"."name";
```

For further usage examples conduct the test cases. 

## Why?

Traditional JPA implementations like Hibernate or Eclipse Link use JPQL as intermediate language,
which is powerful but is an abstraction layer on top of SQL. Most DBMS offer more capabilities than
JPQL exposes. This leads to two different phenomenons, either the developers try to get as far as
possible with JPA and implement the missing functionality in code, leading to inefficiencies due to
not using the DBMS to its full potential, or write raw SQL statements in code which are harder to
maintain and cause headaches when the DAO changes and SQL statements depending on these DAO may
get forgotten and cause errors and slowing down development of new features.

JPA-to-SQL tries to bridde that gap. It drops the target of being database agnostic  and tries
to represent all query features a DBMS has to offer (since most of
the time, development targets one DBMS anyway and as soon as raw SQL statements get in the game, 
DBMS agnosticity goes down the drain).

Currently, the main target DBMS of the library is PostgreSQL. Though implementing another target DBMS
should be relatively easy, with most functionality being already there. Adding additional functions
should be easy and straightforward as well.

Ideally, the library will also work with CriteriaQuery's not build with the libraries implementation
of JPA Critera, allowing coexistence with other JPA implementations.

### Notice

This library is still a work in progress. I try to grow it as needed, the goal is not to provide a
finished library yet. At one point it may will be a full JPA implementation, but it is not guaranteed.
If you are missing certain functionality, you are more than welcome to contribute to the library. If
you have trouble implementing anything, feel free to ask. The library being approachable and extendable
is an important goal for me, so anyone reporting issues while implementing helps me fulfill this target
and improve the library.

## Implemented

* Basic select statements
* Parameterized queries for PostgreSQL 

## Not yet implemented

* Insert statements
* Update statements
* Delete statements
* Parameterized queries for anything besides PostgreSQL