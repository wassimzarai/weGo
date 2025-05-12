package models;

/**
 * Classe qui représente une réponse d'API standard
 * Permet de renvoyer un message utilisateur en plus des données
 */
public class ApiResponse {
    
    private String message;
    private Object data;
    private boolean success;
    private int statusCode;
    
    // Constructeur pour les réponses simples avec message uniquement
    public ApiResponse(String message, boolean success, int statusCode) {
        this.message = message;
        this.success = success;
        this.statusCode = statusCode;
        this.data = null;
    }
    
    // Constructeur pour les réponses avec données
    public ApiResponse(String message, Object data, boolean success, int statusCode) {
        this.message = message;
        this.data = data;
        this.success = success;
        this.statusCode = statusCode;
    }
    
    // Getters et setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    // Méthodes de fabrique pour créer facilement des réponses standard
    
    /**
     * Crée une réponse de succès avec un message personnalisé
     */
    public static ApiResponse success(String message) {
        return new ApiResponse(message, true, 200);
    }
    
    /**
     * Crée une réponse de succès avec un message et des données
     */
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(message, data, true, 200);
    }
    
    /**
     * Crée une réponse d'erreur pour une demande invalide (400)
     */
    public static ApiResponse badRequest(String message) {
        return new ApiResponse(message, false, 400);
    }
    
    /**
     * Crée une réponse d'erreur pour un élément non trouvé (404)
     */
    public static ApiResponse notFound(String message) {
        return new ApiResponse(message, false, 404);
    }
    
    /**
     * Crée une réponse d'erreur pour un accès non autorisé (401)
     */
    public static ApiResponse unauthorized(String message) {
        return new ApiResponse(message, false, 401);
    }
    
    /**
     * Crée une réponse d'erreur serveur (500)
     */
    public static ApiResponse serverError(String message) {
        return new ApiResponse(message, false, 500);
    }
} 