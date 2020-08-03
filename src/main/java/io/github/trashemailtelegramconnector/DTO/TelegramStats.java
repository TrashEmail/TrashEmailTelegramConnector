package io.github.trashemailtelegramconnector.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelegramStats {
    private String connectorName;

    private Long activeUsers;
    private Long totalNumberOfUsers;
    private Long activeEmailIds;
    private Long totalNumberOfEmailIds;

    public TelegramStats(){
        this.connectorName = "telegram";
    }
}
