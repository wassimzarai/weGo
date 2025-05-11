package services;

import Entites.Conversation;
import Entites.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MessagingService {
    private static List<Conversation> conversations = new ArrayList<>();
    private static int nextConversationId = 1;
    private static int nextMessageId = 1;

    public Conversation createConversation(int tripId, int passengerId, int driverId, 
                                         String passengerName, String driverName) {
        Conversation conversation = new Conversation(tripId, passengerId, driverId, 
                                                   passengerName, driverName);
        conversation.setId(nextConversationId++);
        conversations.add(conversation);
        return conversation;
    }

    public Message sendMessage(int conversationId, int senderId, String content, String senderName) {
        Conversation conversation = getConversationById(conversationId);
        if (conversation == null) {
            return null;
        }

        Message message = new Message(content, senderId, conversationId, senderName);
        message.setId(nextMessageId++);
        
        if (conversation.getMessages() == null) {
            conversation.setMessages(new ArrayList<>());
        }
        conversation.getMessages().add(message);
        
        // Incrémenter le compteur de messages non lus pour l'autre participant
        if (senderId == conversation.getPassengerId()) {
            conversation.incrementUnreadCount();
        }
        
        return message;
    }

    public List<Conversation> getUserConversations(int userId) {
        return conversations.stream()
                .filter(c -> c.getPassengerId() == userId || c.getDriverId() == userId)
                .collect(Collectors.toList());
    }

    public Conversation getConversationById(int conversationId) {
        return conversations.stream()
                .filter(c -> c.getId() == conversationId)
                .findFirst()
                .orElse(null);
    }

    public List<Message> getConversationMessages(int conversationId) {
        Conversation conversation = getConversationById(conversationId);
        return conversation != null ? conversation.getMessages() : new ArrayList<>();
    }

    public void markMessagesAsRead(int conversationId, int userId) {
        Conversation conversation = getConversationById(conversationId);
        if (conversation != null && conversation.getMessages() != null) {
            conversation.getMessages().stream()
                    .filter(m -> m.getSenderId() != userId)
                    .forEach(m -> m.setRead(true));
            conversation.resetUnreadCount();
        }
    }

    public int getUnreadCount(int conversationId, int userId) {
        Conversation conversation = getConversationById(conversationId);
        if (conversation == null || conversation.getMessages() == null) {
            return 0;
        }
        return (int) conversation.getMessages().stream()
                .filter(m -> m.getSenderId() != userId && !m.isRead())
                .count();
    }
} 