package io.github.trashemailtelegramconnector.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrashEmailServiceResponse {
    private Boolean created;
    private String message;
    private String emailId;
    private String error;
    private String isDeleted;
}
