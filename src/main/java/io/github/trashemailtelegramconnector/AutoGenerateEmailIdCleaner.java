package io.github.trashemailtelegramconnector;

import io.github.trashemailtelegramconnector.models.UsedUserId;
import io.github.trashemailtelegramconnector.models.User;
import io.github.trashemailtelegramconnector.repository.FreeUserIdRepository;
import io.github.trashemailtelegramconnector.repository.UsedUserIdRepository;
import io.github.trashemailtelegramconnector.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class AutoGenerateEmailIdCleaner {
    @Autowired
    FreeUserIdRepository freeUserIdRepository;
    @Autowired
    UsedUserIdRepository usedUserIdRepository;
    @Autowired
    TelegramRequestHandler telegramRequestHandler;
    @Autowired
    UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(
            TelegramRequestHandler.class);

    /*
    This will run every 1 min.
    */
    @Scheduled(cron = "0 */1 * * * *")
    public void cleanGeneratedIds() throws Exception {
        log.info("Performing clean ...");

        List<UsedUserId> toDelete = usedUserIdRepository.getUserIdsCreatedBeforeTenMinutes();

        for (UsedUserId usedUserId : toDelete) {
            User user = userRepository.findByEmailIdAndIsActiveTrue(usedUserId.getUserId());
            telegramRequestHandler.deleteEmail(user);
        }
    }
}
