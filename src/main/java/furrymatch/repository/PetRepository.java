package furrymatch.repository;

import furrymatch.domain.Pet;
import furrymatch.domain.Photo;
import furrymatch.domain.SearchCriteria;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Pet entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    @Query(value = "SELECT * FROM Pet WHERE owner_user_id = :ownerId", nativeQuery = true)
    List<Pet> findAllByOwnerID(@Param("ownerId") Long ownerId);

    @Query(
        value = "SELECT jm.id AS matchId " +
        "FROM pet p " +
        "JOIN likee l1 ON (p.id = l1.first_pet_id OR p.id = l1.second_pet_id) " +
        "JOIN likee l2 ON (p.id = l2.first_pet_id OR p.id = l2.second_pet_id) " +
        "JOIN jhi_match jm ON (l1.id = jm.first_liked_id AND l2.id = jm.second_liked_id) " +
        "WHERE p.id = :petId",
        nativeQuery = true
    )
    Optional<Long> findMatchByPetId(@Param("petId") Long petId);
}
