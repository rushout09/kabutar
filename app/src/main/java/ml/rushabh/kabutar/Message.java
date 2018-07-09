package ml.rushabh.kabutar;

public class Message {
    private String messageText;
    private String sender;
    Message(){

    }
    Message(String messageText,String sender){
        this.messageText = messageText;
        this.sender = sender;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getSender() {
        return sender;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
