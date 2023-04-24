package furrymatch.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Chat.
 */
@Entity
@Table(name = "chat")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Chat implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "date_chat")
    private LocalDateTime dateChat;

    @Column(name = "message")
    private String message;

    @Column(name = "state_chat")
    private String stateChat;

    @ManyToOne
    @JsonIgnoreProperties(value = { "contract", "chats", "firstLiked", "secondLiked" }, allowSetters = true)
    private Match match;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Chat id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateChat() {
        return this.dateChat;
    }

    public Chat dateChat(LocalDateTime dateChat) {
        this.setDateChat(dateChat);
        return this;
    }

    public void setDateChat(LocalDateTime dateChat) {
        this.dateChat = dateChat;
    }

    public String getMessage() {
        return this.message;
    }

    public Chat message(String message) {
        this.setMessage(message);
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStateChat() {
        return this.stateChat;
    }

    public Chat stateChat(String stateChat) {
        this.setStateChat(stateChat);
        return this;
    }

    public void setStateChat(String stateChat) {
        this.stateChat = stateChat;
    }

    public Match getMatch() {
        return this.match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Chat match(Match match) {
        this.setMatch(match);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Chat)) {
            return false;
        }
        return id != null && id.equals(((Chat) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Chat{" +
            "id=" + getId() +
            ", dateChat='" + getDateChat() + "'" +
            ", message='" + getMessage() + "'" +
            ", stateChat='" + getStateChat() + "'" +
            "}";
    }
}
