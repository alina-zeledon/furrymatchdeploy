package furrymatch.repository;

import furrymatch.domain.Pet;
import furrymatch.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    String USERS_BY_LOGIN_CACHE = "usersByLogin";

    String USERS_BY_EMAIL_CACHE = "usersByEmail";
    Optional<User> findOneByActivationKey(String activationKey);
    List<User> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant dateTime);
    Optional<User> findOneByResetKey(String resetKey);
    Optional<User> findOneByEmailIgnoreCase(String email);
    Optional<User> findOneByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_LOGIN_CACHE)
    Optional<User> findOneWithAuthoritiesByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_EMAIL_CACHE)
    Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String email);

    Page<User> findAllByIdNotNullAndActivatedIsTrue(Pageable pageable);

    @Modifying
    @Query(value = "UPDATE jhi_user SET image_url = :petId WHERE id = :id", nativeQuery = true)
    void updateUserSelectedPet(@Param("petId") Long petId, @Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE jhi_user SET last_name = :matchPetId WHERE id = :id", nativeQuery = true)
    void updateUserMatchPet(@Param("matchPetId") String matchPetId, @Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE jhi_user SET first_name = :matchId WHERE id = :id", nativeQuery = true)
    void updateUserMatch(@Param("matchId") String matchId, @Param("id") Long id);

    @Query(value = "SELECT * FROM jhi_user WHERE id = :id", nativeQuery = true)
    Optional<User> findOne(@Param("id") Long id);
}
