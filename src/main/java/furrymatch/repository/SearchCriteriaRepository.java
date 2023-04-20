package furrymatch.repository;

import furrymatch.domain.SearchCriteria;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SearchCriteria entity.
 */
@Repository
public interface SearchCriteriaRepository extends JpaRepository<SearchCriteria, Long> {
    @Query(value = "SELECT * FROM search_criteria WHERE pet_id = :id", nativeQuery = true)
    SearchCriteria findByPetId(@Param("id") Long id);

    @Query(value = "SELECT * FROM search_criteria WHERE pet_id = :id", nativeQuery = true)
    Optional<SearchCriteria> findByOwnerUser(@Param("id") Long id);
}
