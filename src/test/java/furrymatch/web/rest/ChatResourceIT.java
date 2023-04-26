package furrymatch.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import furrymatch.IntegrationTest;
import furrymatch.domain.Chat;
import furrymatch.repository.ChatRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ChatResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ChatResourceIT {

    private static final LocalDate DEFAULT_DATE_CHAT = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE_CHAT = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE = "BBBBBBBBBB";

    private static final String DEFAULT_STATE_CHAT = "AAAAAAAAAA";
    private static final String UPDATED_STATE_CHAT = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/chats";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restChatMockMvc;

    private Chat chat;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Chat createEntity(EntityManager em) {
        Chat chat = new Chat().dateChat(LocalDateTime.from(DEFAULT_DATE_CHAT)).message(DEFAULT_MESSAGE).stateChat(DEFAULT_STATE_CHAT);
        return chat;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Chat createUpdatedEntity(EntityManager em) {
        Chat chat = new Chat().dateChat(LocalDateTime.from(UPDATED_DATE_CHAT)).message(UPDATED_MESSAGE).stateChat(UPDATED_STATE_CHAT);
        return chat;
    }

    @BeforeEach
    public void initTest() {
        chat = createEntity(em);
    }

    @Test
    @Transactional
    void createChat() throws Exception {
        int databaseSizeBeforeCreate = chatRepository.findAll().size();
        // Create the Chat
        restChatMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(chat))
            )
            .andExpect(status().isCreated());

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll();
        assertThat(chatList).hasSize(databaseSizeBeforeCreate + 1);
        Chat testChat = chatList.get(chatList.size() - 1);
        assertThat(testChat.getDateChat()).isEqualTo(DEFAULT_DATE_CHAT);
        assertThat(testChat.getMessage()).isEqualTo(DEFAULT_MESSAGE);
        assertThat(testChat.getStateChat()).isEqualTo(DEFAULT_STATE_CHAT);
    }

    @Test
    @Transactional
    void createChatWithExistingId() throws Exception {
        // Create the Chat with an existing ID
        chat.setId(1L);

        int databaseSizeBeforeCreate = chatRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restChatMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(chat))
            )
            .andExpect(status().isBadRequest());

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll();
        assertThat(chatList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllChats() throws Exception {
        // Initialize the database
        chatRepository.saveAndFlush(chat);

        // Get all the chatList
        restChatMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(chat.getId().intValue())))
            .andExpect(jsonPath("$.[*].dateChat").value(hasItem(DEFAULT_DATE_CHAT.toString())))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE)))
            .andExpect(jsonPath("$.[*].stateChat").value(hasItem(DEFAULT_STATE_CHAT)));
    }

    @Test
    @Transactional
    void getChat() throws Exception {
        // Initialize the database
        chatRepository.saveAndFlush(chat);

        // Get the chat
        restChatMockMvc
            .perform(get(ENTITY_API_URL_ID, chat.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(chat.getId().intValue()))
            .andExpect(jsonPath("$.dateChat").value(DEFAULT_DATE_CHAT.toString()))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE))
            .andExpect(jsonPath("$.stateChat").value(DEFAULT_STATE_CHAT));
    }

    @Test
    @Transactional
    void getNonExistingChat() throws Exception {
        // Get the chat
        restChatMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingChat() throws Exception {
        // Initialize the database
        chatRepository.saveAndFlush(chat);

        int databaseSizeBeforeUpdate = chatRepository.findAll().size();

        // Update the chat
        Chat updatedChat = chatRepository.findById(chat.getId()).get();
        // Disconnect from session so that the updates on updatedChat are not directly saved in db
        em.detach(updatedChat);
        updatedChat.dateChat(LocalDateTime.from(UPDATED_DATE_CHAT)).message(UPDATED_MESSAGE).stateChat(UPDATED_STATE_CHAT);

        restChatMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedChat.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedChat))
            )
            .andExpect(status().isOk());

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
        Chat testChat = chatList.get(chatList.size() - 1);
        assertThat(testChat.getDateChat()).isEqualTo(UPDATED_DATE_CHAT);
        assertThat(testChat.getMessage()).isEqualTo(UPDATED_MESSAGE);
        assertThat(testChat.getStateChat()).isEqualTo(UPDATED_STATE_CHAT);
    }

    @Test
    @Transactional
    void putNonExistingChat() throws Exception {
        int databaseSizeBeforeUpdate = chatRepository.findAll().size();
        chat.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restChatMockMvc
            .perform(
                put(ENTITY_API_URL_ID, chat.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(chat))
            )
            .andExpect(status().isBadRequest());

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchChat() throws Exception {
        int databaseSizeBeforeUpdate = chatRepository.findAll().size();
        chat.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(chat))
            )
            .andExpect(status().isBadRequest());

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamChat() throws Exception {
        int databaseSizeBeforeUpdate = chatRepository.findAll().size();
        chat.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(chat))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateChatWithPatch() throws Exception {
        // Initialize the database
        chatRepository.saveAndFlush(chat);

        int databaseSizeBeforeUpdate = chatRepository.findAll().size();

        // Update the chat using partial update
        Chat partialUpdatedChat = new Chat();
        partialUpdatedChat.setId(chat.getId());

        partialUpdatedChat.dateChat(LocalDateTime.from(UPDATED_DATE_CHAT)).stateChat(UPDATED_STATE_CHAT);

        restChatMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedChat.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedChat))
            )
            .andExpect(status().isOk());

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
        Chat testChat = chatList.get(chatList.size() - 1);
        assertThat(testChat.getDateChat()).isEqualTo(UPDATED_DATE_CHAT);
        assertThat(testChat.getMessage()).isEqualTo(DEFAULT_MESSAGE);
        assertThat(testChat.getStateChat()).isEqualTo(UPDATED_STATE_CHAT);
    }

    @Test
    @Transactional
    void fullUpdateChatWithPatch() throws Exception {
        // Initialize the database
        chatRepository.saveAndFlush(chat);

        int databaseSizeBeforeUpdate = chatRepository.findAll().size();

        // Update the chat using partial update
        Chat partialUpdatedChat = new Chat();
        partialUpdatedChat.setId(chat.getId());

        partialUpdatedChat.dateChat(LocalDateTime.from(UPDATED_DATE_CHAT)).message(UPDATED_MESSAGE).stateChat(UPDATED_STATE_CHAT);

        restChatMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedChat.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedChat))
            )
            .andExpect(status().isOk());

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
        Chat testChat = chatList.get(chatList.size() - 1);
        assertThat(testChat.getDateChat()).isEqualTo(UPDATED_DATE_CHAT);
        assertThat(testChat.getMessage()).isEqualTo(UPDATED_MESSAGE);
        assertThat(testChat.getStateChat()).isEqualTo(UPDATED_STATE_CHAT);
    }

    @Test
    @Transactional
    void patchNonExistingChat() throws Exception {
        int databaseSizeBeforeUpdate = chatRepository.findAll().size();
        chat.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restChatMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, chat.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(chat))
            )
            .andExpect(status().isBadRequest());

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchChat() throws Exception {
        int databaseSizeBeforeUpdate = chatRepository.findAll().size();
        chat.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(chat))
            )
            .andExpect(status().isBadRequest());

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamChat() throws Exception {
        int databaseSizeBeforeUpdate = chatRepository.findAll().size();
        chat.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(chat))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteChat() throws Exception {
        // Initialize the database
        chatRepository.saveAndFlush(chat);

        int databaseSizeBeforeDelete = chatRepository.findAll().size();

        // Delete the chat
        restChatMockMvc
            .perform(delete(ENTITY_API_URL_ID, chat.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Chat> chatList = chatRepository.findAll();
        assertThat(chatList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
