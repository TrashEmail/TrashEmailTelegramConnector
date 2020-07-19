package io.github.trashemailtelegramconnector.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EmailMessage {
    private String emailId;
    private String message;
    private Date arrived;
    private String emailURI;
    private String emailDownloadPath;
    private List<String> attachmentsPaths;
}
