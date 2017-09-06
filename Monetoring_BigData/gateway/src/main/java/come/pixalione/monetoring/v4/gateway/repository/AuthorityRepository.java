package come.pixalione.monetoring.v4.gateway.repository;

import come.pixalione.monetoring.v4.gateway.domain.Authority;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Authority entity.
 */
public interface AuthorityRepository extends MongoRepository<Authority, String> {
}
