package com.atlas.Psikosys.service;

import com.atlas.Psikosys.entity.Chat;
import com.atlas.Psikosys.entity.Message;
import com.atlas.Psikosys.entity.User;
import com.atlas.Psikosys.repository.ChatRepository;
import com.atlas.Psikosys.repository.MessageRepository;
import com.atlas.Psikosys.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing chat-related operations.
 */
@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final AIService aiService;
    private final MessageRepository messageRepository;

    public ChatService(ChatRepository chatRepository, UserRepository userRepository, AIService aiService, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
        this.messageRepository = messageRepository;
    }

    /**
     * Creates a new chat for the current user.
     * @return The created Chat object.
     */
    public Chat createChat() {
        User currentUser = getCurrentUser();

        // Create new chat
        Chat chat = new Chat();
        chat.setUser(currentUser);

        // Save chat and return
        return chatRepository.save(chat);
    }

    public void saveChat(Chat chat) {
        chatRepository.save(chat);
    }

    /**
     * Finds a chat by its ID.
     * @param id The UUID of the chat.
     * @return The Chat object.
     * @throws RuntimeException if the chat is not found.
     */
    public Chat findChatById(UUID id) {
        return chatRepository.findById(id).orElseThrow(() -> new RuntimeException("Chat not found" + id));
    }

    /**
     * Retrieves the current authenticated user.
     * @return The User object.
     * @throws RuntimeException if the user is not authenticated.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            String email = oauth2User.getAttribute("email");
            if (email != null) {
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }
        }

        throw new RuntimeException("Not authenticated or invalid user.");
    }

    /**
     * Adds a message to a chat.
     * @param chatId The UUID of the chat.
     * @param content The content of the message.
     * @param isUser Whether the message is from the user.
     */
    public void addMessageToChat(UUID chatId, String content, boolean isUser) {
        Chat chat = findChatById(chatId);

        Message message = new Message();
        message.setContent(content);
        message.setChat(chat);
        message.setIsUser(isUser);

        // Önce message'ı kaydet
        messageRepository.save(message);

        // Sonra chat'e ekle
        chat.getMessages().add(message);

        // Generate a title for the chat if it's the first user message
        if (isUser && chat.getMessages().size() == 1) {
            try {
                String title = aiService.generateChatTitle(content);
                chat.setTitle(title);
                chatRepository.save(chat);
            } catch (Exception e) {
                chat.setTitle("Chat " + LocalDate.now());
                chatRepository.save(chat);
            }
        }
    }

    /**
     * Finds a chat by its ID and orders its messages by creation time.
     * @param chatId The UUID of the chat.
     * @return The Chat object with ordered messages.
     */
    public Chat findChatByIdWithOrderedMessages(UUID chatId) {
        Chat chat = findChatById(chatId);

        List<Message> orderedMessages = chat.getMessages().stream()
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .collect(Collectors.toList());

        chat.setMessages(new ArrayList<>(orderedMessages));
        return chat;
    }

    public List<Message> getMessagesByChatId(UUID chatId) {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
    }

    public void updateChatPersonality(UUID chatId, String personality) {
        Chat chat = findChatById(chatId);
        chat.setSelectedPersonality(personality);
        chatRepository.save(chat);
    }
}