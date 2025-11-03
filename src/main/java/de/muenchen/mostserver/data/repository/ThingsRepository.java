package de.muenchen.mostserver.data.repository;

import de.muenchen.mostserver.data.dao.ThingDao;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Repository
public interface ThingsRepository extends ReactiveCrudRepository<ThingDao, UUID> {

    default Mono<ThingDao> create(ThingDao t) {
        return create(t.getName());
    }

    @Query(value = "insert into Thing(name) values(:name) RETURNING *")
    Mono<ThingDao> create(String name);
}
