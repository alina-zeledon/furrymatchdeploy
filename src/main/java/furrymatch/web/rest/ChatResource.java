package furrymatch.web.rest;

import furrymatch.domain.Chat;
import furrymatch.domain.Match;
import furrymatch.repository.ChatRepository;
import furrymatch.repository.UserRepository;
import furrymatch.repository.UserRepository;
import furrymatch.security.SecurityUtils;
import furrymatch.service.ChatService;
import furrymatch.service.MatchService;
import furrymatch.service.UserService;
import furrymatch.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link furrymatch.domain.Chat}.
 */
@RestController
@RequestMapping("/api")
public class ChatResource {

    private final Logger log = LoggerFactory.getLogger(ChatResource.class);

    private static final String ENTITY_NAME = "chat";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ChatService chatService;

    private final ChatRepository chatRepository;

    private final UserService userService;

    private final MatchService matchService;

    private final UserRepository userRepository;

    private Match match;

    public ChatResource(
        ChatService chatService,
        ChatRepository chatRepository,
        UserService userService,
        UserRepository userRepository,
        MatchService matchService
    ) {
        this.chatService = chatService;
        this.chatRepository = chatRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.matchService = matchService;
    }

    /**
     * {@code POST  /chats} : Create a new chat.
     *
     * @param chat the chat to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new chat, or with status {@code 400 (Bad Request)} if the chat has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/chats")
    public ResponseEntity<Chat> createChat(@RequestBody Chat chat) throws URISyntaxException {
        log.debug("REST request to save Chat : {}", chat);
        if (chat.getId() != null) {
            throw new BadRequestAlertException("A new chat cannot already have an ID", ENTITY_NAME, "idexists");
        }
        chat.setDateChat(LocalDateTime.now());
        Chat result = chatService.save(chat);
        return ResponseEntity
            .created(new URI("/api/chats/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @PostMapping("/chats/empty")
    public ResponseEntity<Chat> createChatEmpty(@RequestBody Long id) throws URISyntaxException {
        Chat chat = new Chat();
        chat.setDateChat(LocalDateTime.now());
        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(userr -> {
                match = matchService.findOne(Long.valueOf((userr.getFirstName()).substring(0, (userr.getFirstName()).indexOf(",")))).get();
                chat.setMatch(match);
            });
        Chat result = chatService.save(chat);
        return ResponseEntity
            .created(new URI("/api/chats/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /chats/:id} : Updates an existing chat.
     *
     * @param id the id of the chat to save.
     * @param chat the chat to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated chat,
     * or with status {@code 400 (Bad Request)} if the chat is not valid,
     * or with status {@code 500 (Internal Server Error)} if the chat couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/chats/{id}")
    public ResponseEntity<Chat> updateChat(@PathVariable(value = "id", required = false) final Long id, @RequestBody Chat chat)
        throws URISyntaxException {
        log.debug("REST request to update Chat : {}, {}", id, chat);
        if (chat.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, chat.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!chatRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Chat result = chatService.update(chat);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, chat.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /chats/:id} : Partial updates given fields of an existing chat, field will ignore if it is null
     *
     * @param id the id of the chat to save.
     * @param chat the chat to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated chat,
     * or with status {@code 400 (Bad Request)} if the chat is not valid,
     * or with status {@code 404 (Not Found)} if the chat is not found,
     * or with status {@code 500 (Internal Server Error)} if the chat couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/chats/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Chat> partialUpdateChat(@PathVariable(value = "id", required = false) final Long id, @RequestBody Chat chat)
        throws URISyntaxException {
        log.debug("REST request to partial update Chat partially : {}, {}", id, chat);
        if (chat.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, chat.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!chatRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Chat> result = chatService.partialUpdate(chat);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, chat.getId().toString())
        );
    }

    /**
     * {@code GET  /chats} : get all the chats.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of chats in body.
     */
    @GetMapping("/chats")
    public ResponseEntity<List<Chat>> getAllChats(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Chats");
        Page<Chat> page = chatService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/chats/state")
    public ResponseEntity<List<Chat>> getChatsByStateChat(@RequestParam String state1, @RequestParam String state2) {
        log.debug("REST request to get Chats by state_chat: {} or {}", state1, state2);
        List<Chat> chats = chatService.findByStateChat(state1, state2);
        return ResponseEntity.ok().body(chats);
    }

    /**
     * {@code GET  /chats/:id} : get the "id" chat.
     *
     * @param id the id of the chat to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the chat, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/chats/{id}")
    public ResponseEntity<Chat> getChat(@PathVariable Long id) {
        log.debug("REST request to get Chat : {}", id);
        Optional<Chat> chat = chatService.findOne(id);
        return ResponseUtil.wrapOrNotFound(chat);
    }

    /**
     * {@code DELETE  /chats/:id} : delete the "match_id" chats.
     *
     * @param id the id of the chat to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/chats/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long id) {
        log.debug("REST request to delete Chat : {}", id);
        chatService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @GetMapping("/chats/unread")
    public ResponseEntity<List<Chat>> getUnreadChatsForCurrentUser() {
        Long currentOwnerId = userService.getUserWithAuthorities().get().getId();
        List<Chat> unreadChats = chatService.findUnreadChatsByOwnerId(currentOwnerId);
        return ResponseEntity.ok().body(unreadChats);
    }

    @PutMapping("/chats/update-state/{matchId}/{senderId}")
    public ResponseEntity<Void> updateChatState(@PathVariable Long matchId, @PathVariable Long senderId) {
        chatService.updateChatState(matchId, senderId);
        return ResponseEntity.ok().build();
    }
}
