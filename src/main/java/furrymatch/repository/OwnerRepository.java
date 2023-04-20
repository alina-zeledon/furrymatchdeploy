package furrymatch.repository;

import furrymatch.domain.Owner;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Owner entity.
 */
@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    @Query(
        value = "SELECT user_id, first_name, second_name, first_last_name, second_last_name, q.match_id as identity_number, q.id as phone_number, photo, address, province, canton, district, otp, created_at, updated_at FROM  (SELECT c.id, m.id as match_id, c.date_chat, l.first_pet_id, l.second_pet_id from chat c LEFT JOIN jhi_match m ON c.match_id = m.id LEFT JOIN likee l ON ( m.first_liked_id = l.id OR m.second_liked_id = l.id) WHERE l.first_pet_id = :petId OR l.second_pet_id = :petId order by c.date_chat DESC) q INNER JOIN pet p ON q.first_pet_id = p.id INNER JOIN owner o ON p.owner_user_id = o.user_id WHERE p.id != :petId",
        nativeQuery = true
    )
    List<Owner> findUserPetChats(@Param("petId") Long petId);
}
