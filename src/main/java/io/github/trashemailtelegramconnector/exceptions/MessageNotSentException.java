package io.github.trashemailtelegramconnector.exceptions;

public class MessageNotSentException extends Exception {
    public MessageNotSentException() {
        super("Message not sent to user.");
    }
}
