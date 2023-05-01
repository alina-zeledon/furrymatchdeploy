package furrymatch.repository;

import furrymatch.domain.Photo;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Photo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    @Query(value = "SELECT * FROM Photo WHERE pet_id = :petId", nativeQuery = true)
    List<Photo> findAllPhotosByPetID(@Param("petId") Long petId);

    void deleteByIdIn(List<Long> ids);
}
