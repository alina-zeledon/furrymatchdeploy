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

    @Query(
        value = "SELECT * FROM chat WHERE substring_index(state_chat, ';', -1) = 'unread' AND substring_index(substring_index(state_chat, ';', -2), ';', 1) = :ownerId",
        nativeQuery = true
    )
    List<Chat> findUnreadChatsByOwnerId(@Param("ownerId") Long ownerId);

    //Marks chat message as read
    @Modifying
    @Query(
        "UPDATE Chat c SET c.stateChat = REPLACE(c.stateChat, 'unread', 'read') WHERE c.match.id = :matchId AND LOCATE(CONCAT(:senderId, ';'), c.stateChat) = 1 AND c.stateChat LIKE '%unread'"
    )
    void updateChatStateByMatchIdAndRecipientId(@Param("matchId") Long matchId, @Param("senderId") Long senderId);
}
