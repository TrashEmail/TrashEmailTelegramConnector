package io.github.trashemailtelegramconnector.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@NoArgsConstructor
@Configuration
public class TrashEmailConfig {
    private String url;
}
