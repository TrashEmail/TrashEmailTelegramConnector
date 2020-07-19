package io.github.trashemailtelegramconnector.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="user_email_to_chat_id_mapping")
public class User {
    @Id
    @GeneratedValue
    private Integer id;
    private long chatId;

    private String emailId;

    private String forwardsTo;

    @CreationTimestamp
    private LocalDateTime createDateTime;

    private Boolean isActive;

    public User(long chatId, String emailId, String forwardsTo,
                Boolean isActive) {
        this.chatId = chatId;
        this.emailId = emailId;
        this.forwardsTo = forwardsTo;
        this.isActive = isActive;
    }

    public User(long chatId, String emailId, String forwardsTo) {
        this(chatId, emailId, forwardsTo, Boolean.TRUE);
    }

    @Override
    public java.lang.String toString() {
        return this.emailId;
    }
}
