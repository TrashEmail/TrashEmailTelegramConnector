package io.github.trashemailtelegramconnector.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrashEmailServiceRequest {
    private String requestPath;
    private String source;
    private String destination;
    private String destinationType;
    private String emailId;
    private Boolean isActive;
}
