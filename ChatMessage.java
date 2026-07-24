package careplus.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String senderId;
    private String receiverRole;
    private String message;
    private LocalDateTime sentAt;

    public ChatMessage() {
    }

    public ChatMessage(String senderId, String receiverRole, String message, LocalDateTime sentAt) {
        this.senderId = senderId;
        this.receiverRole = receiverRole;
        this.message = message;
        this.sentAt = sentAt;
    }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getReceiverRole() { return receiverRole; }
    public void setReceiverRole(String receiverRole) { this.receiverRole = receiverRole; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
