package io.github.trashemailtelegramconnector.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.trashemailtelegramconnector.config.TelegramConnectorConfig;
import io.github.trashemailtelegramconnector.models.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrashEmailServiceRequest {
    @JsonIgnore
    @Autowired
    private TelegramConnectorConfig telegramConnectorConfig;

    private String requestPath;
    private String source;
    private String destination;
    private String destinationType;
    private String emailId;
    private Boolean isActive;

    public TrashEmailServiceRequest(User user) {
        this.source = telegramConnectorConfig.getSource();
        this.destination = telegramConnectorConfig.getSource() + "/sendMessage";
        this.destinationType = "telegram";
        this.emailId = user.getEmailId();
        this.isActive = user.getIsActive();
    }
}
