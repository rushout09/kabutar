package ml.rushabh.kabutar;

import java.util.Date;

public class Message {
    private String messageText;
    private String sender;
    private long messageTime;
    private String photoUrl;
    Message(){

    }
    Message(String messageText,String sender, String photoUrl){
        this.messageText = messageText;
        this.sender = sender;
        this.photoUrl = photoUrl;
        messageTime = new Date().getTime();
    }

    public String getMessageText() {
        return messageText;
    }

    public String getSender() {
        return sender;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
