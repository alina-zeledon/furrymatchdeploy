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

    @Query("SELECT c FROM Chat c WHERE c.stateChat = :state1 OR c.stateChat = :state2")
    List<Chat> findByStateChat(@Param("state1") String state1, @Param("state2") String state2);
}
