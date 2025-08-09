package com.example.techiedating.service;

import com.example.techiedating.model.ChatMessage;
import com.example.techiedating.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    
    /**
     * Save a new chat message
     */
    @Transactional
    public ChatMessage saveMessage(ChatMessage message) {
        // Generate conversation ID if not provided
        if (message.getConversationId() == null) {
            String conversationId = ChatMessage.generateConversationId(
                    message.getSenderId(),
                    message.getRecipientId()
            );
            message.setConversationId(conversationId);
        }
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(now);
        }
        message.setUpdatedAt(now);
        
        return chatMessageRepository.save(message);
    }
    
    /**
     * Get conversation between two users
     */
    @Cacheable(value = "conversation", key = "#userId1 + '_' + #userId2 + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ChatMessage> getConversation(String userId1, String userId2, Pageable pageable) {
        String conversationId = ChatMessage.generateConversationId(userId1, userId2);
        return chatMessageRepository.findByConversationId(conversationId, pageable);
    }
    
    /**
     * Get all conversations for a user
     */
    @Cacheable(value = "userConversations", key = "#userId")
    public List<ChatMessage> getUserConversations(String userId) {
        return chatMessageRepository.findRecentConversations(userId);
    }
    
    /**
     * Mark a message as read
     */
    @Transactional
    @CacheEvict(value = {"conversation", "userConversations"}, allEntries = true)
    public void markMessageAsRead(String messageId, String userId) {
        chatMessageRepository.findById(messageId).ifPresent(message -> {
            if (message.getRecipientId().equals(userId)) {
                message.setRead(true);
                chatMessageRepository.save(message);
                log.info("Marked message {} as read by user {}", messageId, userId);
            }
        });
    }
    
    /**
     * Mark all messages in a conversation as read
     */
    @Transactional
    @CacheEvict(value = {"conversation", "userConversations"}, allEntries = true)
    public void markConversationAsRead(String conversationId, String userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository
                .findByConversationIdAndRecipientIdAndIsReadFalse(conversationId, userId);
        
        unreadMessages.forEach(message -> message.setRead(true));
        chatMessageRepository.saveAll(unreadMessages);
        
        log.info("Marked {} messages as read in conversation {} for user {}", 
                unreadMessages.size(), conversationId, userId);
    }
    
    /**
     * Get unread message count for a user
     */
    @Cacheable(value = "unreadCount", key = "#userId")
    public long getUnreadMessageCount(String userId) {
        return chatMessageRepository.countByRecipientIdAndIsReadFalse(userId);
    }
}
