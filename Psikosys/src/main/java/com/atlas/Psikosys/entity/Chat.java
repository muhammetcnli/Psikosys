package com.atlas.Psikosys.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @JdbcTypeCode(java.sql.Types.VARCHAR)
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // empty constructor, createdAt initialization
    public Chat() {
        // make createdAt now
        this.createdAt = LocalDate.now();
    }

    // getters
    public String getTitle() {
        return title;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public User getUser() {
        return user;
    }

    // setter

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void removeMessage(Message message) {
        messages.remove(message);
    }
}
