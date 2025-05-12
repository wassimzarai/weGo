package controllers;

import entities.Conversation;
import entities.Message;
import services.MessagingService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/messaging")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessagingController {
    private final MessagingService messagingService = new MessagingService();

    @POST
    @Path("/conversations")
    public Response createConversation(
            @QueryParam("tripId") int tripId,
            @QueryParam("passengerId") int passengerId,
            @QueryParam("driverId") int driverId,
            @QueryParam("passengerName") String passengerName,
            @QueryParam("driverName") String driverName) {
        try {
            Conversation conversation = messagingService.createConversation(
                    tripId, passengerId, driverId, passengerName, driverName);
            return Response.ok(conversation).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la création de la conversation: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/messages")
    public Response sendMessage(
            @QueryParam("conversationId") int conversationId,
            @QueryParam("senderId") int senderId,
            @QueryParam("content") String content,
            @QueryParam("senderName") String senderName) {
        try {
            Message message = messagingService.sendMessage(conversationId, senderId, content, senderName);
            if (message == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Conversation non trouvée")
                        .build();
            }
            return Response.ok(message).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de l'envoi du message: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/conversations/user/{userId}")
    public Response getUserConversations(@PathParam("userId") int userId) {
        try {
            List<Conversation> conversations = messagingService.getUserConversations(userId);
            return Response.ok(conversations).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la récupération des conversations: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/conversations/{conversationId}/messages")
    public Response getConversationMessages(@PathParam("conversationId") int conversationId) {
        try {
            List<Message> messages = messagingService.getConversationMessages(conversationId);
            return Response.ok(messages).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la récupération des messages: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/conversations/{conversationId}/read/{userId}")
    public Response markMessagesAsRead(
            @PathParam("conversationId") int conversationId,
            @PathParam("userId") int userId) {
        try {
            messagingService.markMessagesAsRead(conversationId, userId);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors du marquage des messages comme lus: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/conversations/{conversationId}/unread/{userId}")
    public Response getUnreadCount(
            @PathParam("conversationId") int conversationId,
            @PathParam("userId") int userId) {
        try {
            int unreadCount = messagingService.getUnreadCount(conversationId, userId);
            return Response.ok(unreadCount).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la récupération du nombre de messages non lus: " + e.getMessage())
                    .build();
        }
    }
} 