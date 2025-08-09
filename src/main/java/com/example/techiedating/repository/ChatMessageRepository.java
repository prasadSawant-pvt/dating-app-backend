package com.example.techiedating.repository;

import com.example.techiedating.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    /**
     * Find all messages in a conversation with pagination
     */
    Page<ChatMessage> findByConversationId(String conversationId, Pageable pageable);
    
    /**
     * Find recent conversations for a user (one message per conversation)
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.id IN (SELECT MAX(m2.id) FROM ChatMessage m2 " +
           "WHERE m2.senderId = :userId OR m2.recipientId = :userId " +
           "GROUP BY m2.conversationId) " +
           "ORDER BY m.updatedAt DESC")
    List<ChatMessage> findRecentConversations(@Param("userId") String userId);
    
    /**
     * Find unread messages in a conversation for a specific user
     */
    List<ChatMessage> findByConversationIdAndRecipientIdAndIsReadFalse(
            String conversationId, String recipientId);
    
    /**
     * Count unread messages for a user
     */
    long countByRecipientIdAndIsReadFalse(String recipientId);
    
    /**
     * Find all messages between two users
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE (m.senderId = :user1Id AND m.recipientId = :user2Id) " +
           "OR (m.senderId = :user2Id AND m.recipientId = :user1Id) " +
           "ORDER BY m.createdAt DESC")
    List<ChatMessage> findMessagesBetweenUsers(
            @Param("user1Id") String user1Id,
            @Param("user2Id") String user2Id,
            Pageable pageable);
}
