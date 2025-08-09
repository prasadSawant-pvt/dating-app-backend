package com.example.techiedating.controller;

import com.example.techiedating.model.ChatMessage;
import com.example.techiedating.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;
import java.util.Objects;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @Autowired
    public WebSocketController(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    /**
     * Handle private messages between users
     */
    @MessageMapping("/chat/private/{recipientId}")
    public void sendPrivateMessage(
            @DestinationVariable String recipientId,
            @Payload ChatMessage message,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        
        String senderId = principal.getName();
        message.setSenderId(senderId);
        message.setRecipientId(recipientId);
        
        // Save the message to the database
        ChatMessage savedMessage = chatService.saveMessage(message);
        
        // Send to the recipient
        messagingTemplate.convertAndSendToUser(
                recipientId,
                "/queue/private",
                savedMessage);
        
        // Also send to the sender (for confirmation)
        messagingTemplate.convertAndSendToUser(
                senderId,
                "/queue/private",
                savedMessage);
        
        // Notify users about new message
        notifyNewMessage(recipientId, senderId);
    }
    
    /**
     * Handle typing indicators
     */
    @MessageMapping("/chat/typing")
    @SendToUser("/queue/typing")
    public String handleTyping(
            @Payload String recipientId,
            Principal principal) {
        return principal.getName(); // Return the username of the typing user
    }
    
    /**
     * Handle message read receipts
     */
    @MessageMapping("/chat/read/{messageId}")
    public void handleMessageRead(
            @DestinationVariable String messageId,
            Principal principal) {
        chatService.markMessageAsRead(messageId, principal.getName());
    }
    
    /**
     * Notify a user about a new message
     */
    private void notifyNewMessage(String recipientId, String senderId) {
        // Send notification to the recipient
        messagingTemplate.convertAndSendToUser(
                recipientId,
                "/queue/notifications",
                "New message from: " + senderId);
    }
    
    /**
     * Handle user online/offline status
     */
    @MessageMapping("/user/status")
    @SendTo("/topic/status")
    public String handleUserStatus(
            @Payload String status,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        
        return String.format("%s is %s", principal.getName(), status);
    }
}
