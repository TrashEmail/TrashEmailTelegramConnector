package io.github.trashemailtelegramconnector;

import io.github.trashemailtelegramconnector.config.TelegramConfig;
import io.github.trashemailtelegramconnector.exceptions.MessageNotSentException;
import io.github.trashemailtelegramconnector.telegram.TelegramMessageResponse;
import io.github.trashemailtelegramconnector.telegram.messageEntities.InlineKeyboardButton;
import io.github.trashemailtelegramconnector.telegram.messageEntities.InlineKeyboardMarkup;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@EnableAsync
public class TelegramHelper {

    @Autowired
    TelegramConfig telegramConfig;

    @Autowired
    RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(
            TelegramHelper.class);

    public ArrayList<String> chunks(String message) {
        /*
        Checking the message size and go splitting into chunks if required
        */
        int maxMessageSize = telegramConfig.getSize();

        ArrayList<String> split = new ArrayList<>();
        for (int i = 0; i <= message.length() / maxMessageSize; i++) {
            split.add(message.substring(i * maxMessageSize,
                                        Math.min((i + 1) * maxMessageSize,
                                                 message.length())));
        }
        return split;
    }

    @Async
    public void sendMessgae(Long chatId, String message, String emailURI) {
        String telegramSendMessageURI = telegramConfig.getUrl() + telegramConfig.getBotToken() + "/sendMessage";

        String escapedMessage = StringEscapeUtils.escapeHtml4(message);
        ArrayList<String> messageChunks = chunks(escapedMessage);

        for (int i = 0; i < messageChunks.size(); i++) {
            TelegramMessageResponse request = new TelegramMessageResponse(
                    chatId,
                    messageChunks.get(i),
                    "html");


            ResponseEntity response = restTemplate.postForEntity(
                    telegramSendMessageURI,
                    request,
                    TelegramMessageResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("Message sent to user: " + chatId);
            } else
                log.error("Unable to send message to user: " + chatId);
        }

        if (emailURI != null) {
            /*
            Send HTML button back to user if everything is good with filename.
            */
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();

            InlineKeyboardButton keyboardButton = new InlineKeyboardButton();
            keyboardButton.setText("HTML version");
            keyboardButton.setUrl(emailURI);

            List<List<InlineKeyboardButton>> buttonList = new ArrayList<>();

            buttonList.add(new ArrayList<>());
            buttonList.get(0).add(keyboardButton);

            markupKeyboard.setInlineKeyboardButtonList(buttonList);

            TelegramMessageResponse telegramMessageResponse = new TelegramMessageResponse(chatId,
                            "To view in HTML format click the link below.",
                            markupKeyboard);

            ResponseEntity response = restTemplate.postForEntity(
                    telegramSendMessageURI,
                    telegramMessageResponse,
                    TelegramMessageResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug(String.format("HTML link: %s sent to user: %s", emailURI, chatId));
            } else log.error(String.format("Error sending HTML link: %s sent to user: %s", emailURI, chatId));
        }
    }

    public void sendFile(Long chatId, List<String> filenames) {
        String telegramSendDocumentURI = telegramConfig.getUrl() + telegramConfig.getBotToken() + "/sendDocument";

        for(String filename : filenames){
            File file = new File(filename);

            MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
            try {
                bodyMap.add("document", getFileResource(filename));
                bodyMap.add("chat_id", chatId);
                bodyMap.add("caption", file.getName());
            } catch (IOException e) {
                log.error("Unable to fetch the file: " + filename);
                e.printStackTrace();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    telegramSendDocumentURI,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("File sent to user: " + chatId + filename);
                file.delete();
            } else {
                log.error("Unable to sent file to user: " + chatId + filename);
            }
        }
    }

    public static Resource getFileResource(String location) throws IOException {
        return new FileSystemResource(location);
    }
}
