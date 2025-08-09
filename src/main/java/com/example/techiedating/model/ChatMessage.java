package com.example.techiedating.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "conversation_id", nullable = false)
    private String conversationId;
    
    @Column(name = "sender_id", nullable = false)
    private String senderId;
    
    @Column(name = "recipient_id", nullable = false)
    private String recipientId;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;
    
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        EMOJI,
        SYSTEM
    }
    
    // Helper method to generate conversation ID between two users
    public static String generateConversationId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 
                ? String.format("%s_%s", userId1, userId2)
                : String.format("%s_%s", userId2, userId1);
    }
}
