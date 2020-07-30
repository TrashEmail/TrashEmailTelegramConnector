package io.github.trashemailtelegramconnector.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TelegramStats {
    private Long activeUsers;
    private Long totalNumberOfUsers;
    private Long activeEmailIds;
    private Long totalNumberOfEmailIds;
}
