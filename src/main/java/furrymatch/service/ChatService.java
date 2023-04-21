package furrymatch.service;

import furrymatch.domain.Chat;
import furrymatch.repository.ChatRepository;
import furrymatch.repository.UserRepository;
import furrymatch.security.SecurityUtils;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Chat}.
 */
@Service
@Transactional
public class ChatService {

    private final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatRepository chatRepository;

    private final UserRepository userRepository;

    private Long selectedPet;

    public ChatService(ChatRepository chatRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    /**
     * Save a chat.
     *
     * @param chat the entity to save.
     * @return the persisted entity.
     */
    public Chat save(Chat chat) {
        log.debug("Request to save Chat : {}", chat);
        return chatRepository.save(chat);
    }

    /**
     * Update a chat.
     *
     * @param chat the entity to save.
     * @return the persisted entity.
     */
    public Chat update(Chat chat) {
        log.debug("Request to update Chat : {}", chat);
        return chatRepository.save(chat);
    }

    /**
     * Partially update a chat.
     *
     * @param chat the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Chat> partialUpdate(Chat chat) {
        log.debug("Request to partially update Chat : {}", chat);

        return chatRepository
            .findById(chat.getId())
            .map(existingChat -> {
                if (chat.getDateChat() != null) {
                    existingChat.setDateChat(chat.getDateChat());
                }
                if (chat.getMessage() != null) {
                    existingChat.setMessage(chat.getMessage());
                }
                if (chat.getStateChat() != null) {
                    existingChat.setStateChat(chat.getStateChat());
                }

                return existingChat;
            })
            .map(chatRepository::save);
    }

    /**
     * Get all the chats.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Chat> findAll(Pageable pageable) {
        log.debug("Request to get all Chats");
        return chatRepository.findAll(pageable);
    }

    /**
     * Get one chat by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Chat> findOne(Long id) {
        log.debug("Request to get Chat : {}", id);
        return chatRepository.findById(id);
    }

    /**
     * Delete the chats by match_id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Chat : {}", id);
        // chatRepository.deleteById(id);
        chatRepository.deleteChats(id);
    }
}
