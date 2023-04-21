package furrymatch.repository;

import furrymatch.domain.Chat;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Chat entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Modifying
    @Query(value = "DELETE FROM chat WHERE match_id = :id", nativeQuery = true)
    void deleteChats(@Param("id") Long id);
}
