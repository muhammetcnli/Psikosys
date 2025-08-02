package com.atlas.Psikosys.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDateTime;
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

    private String selectedPersonality;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Constructor
    public Chat() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public String getSelectedPersonality() {
        return selectedPersonality;
    }

    public String getTitle() {
        return title;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public User getUser() {
        return user;
    }

    // Setters
    public void setSelectedPersonality(String selectedPersonality) {
        this.selectedPersonality = selectedPersonality;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addMessage(Message message) {
        messages.add(message);
        this.updatedAt = LocalDateTime.now(); // Chat güncellendiğinde updatedAt'i güncelle
    }

    public void removeMessage(Message message) {
        messages.remove(message);
        this.updatedAt = LocalDateTime.now();
    }
}