package io.github.trashemailtelegramconnector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.trashemailtelegramconnector.DTO.EmailMessage;
import io.github.trashemailtelegramconnector.DTO.MessageSentResponse;
import io.github.trashemailtelegramconnector.config.TelegramConfig;
import io.github.trashemailtelegramconnector.repository.UserRepository;
import io.github.trashemailtelegramconnector.telegram.TelegramMessageResponse;
import io.github.trashemailtelegramconnector.telegram.messageEntities.CallbackQuery;
import io.github.trashemailtelegramconnector.telegram.messageEntities.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
public class TelegramResource {

    @Autowired
    TelegramRequestHandler telegramRequestHandler;

    @Autowired
    TelegramConfig telegramConfig;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TelegramHelper telegramHelper;

    private static final Logger log = LoggerFactory.getLogger(
            TelegramResource.class);

    @PostMapping(value = "/telegram/new-message")
    public TelegramMessageResponse messageHandler(
            @RequestBody JsonNode telegramMessageRequest) {

        Object response = null;
        String responseText = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode jsonCallbackQueryNode =
                    telegramMessageRequest.get("callback_query");

            // This request is callback message or edited message.
            if(jsonCallbackQueryNode != null){
                CallbackQuery callbackQuery =
                        objectMapper.convertValue(jsonCallbackQueryNode,
                                                  CallbackQuery.class);
                // Process callback Query data.
                response = telegramRequestHandler.handleRequest(
                        callbackQuery.getFrom().getId(),
                        callbackQuery.getData()
                );
            }

            // This is Message.
            else{
                JsonNode jsonMessageNode = telegramMessageRequest.get("message");
                if(jsonMessageNode != null) {
                    // this is surely a message/
                    Message message = objectMapper.convertValue(
                            jsonMessageNode, Message.class);
                    // Process message and respond.
                    response = telegramRequestHandler.handleRequest(
                            message.getChat().getId(),
                            message.getText()
                    );
                }
            }


        }
        catch(HttpClientErrorException httpClientException) {
            responseText = httpClientException.getMessage();
            return new TelegramMessageResponse(
                    1,
                    responseText
            );
        }catch(Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return new TelegramMessageResponse(
                    1,
                    responseText
            );
        }

        return (TelegramMessageResponse) response;
    }

    @PostMapping(value = "/telegram/sendMessage")
    public MessageSentResponse sendTelegramMessage(@RequestBody EmailMessage emailMessage) {
        Long chatId = userRepository.findByEmailIdAndIsActiveTrue(emailMessage.getEmailId()).getChatId();

        telegramHelper.sendMessgae(
                chatId,
                emailMessage.getMessage(),
                emailMessage.getEmailURI()
        );
        if(emailMessage.getAttachmentsPaths() != null){
            telegramHelper.sendFile(chatId, emailMessage.getAttachmentsPaths());
        }
        MessageSentResponse msr = new MessageSentResponse();
        msr.setMessageSent(true);
        return msr;
    }

}
