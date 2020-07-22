package io.github.trashemailtelegramconnector.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix="telegram-connector")
public class TelegramConnectorConfig {
     private String source;
     private Integer maxEmailsPerUser;
     private List<String> hosts;
}
