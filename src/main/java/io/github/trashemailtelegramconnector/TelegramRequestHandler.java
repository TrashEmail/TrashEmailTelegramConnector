package io.github.trashemailtelegramconnector;

import io.github.trashemailtelegramconnector.DTO.TrashEmailServiceRequest;
import io.github.trashemailtelegramconnector.DTO.TrashEmailServiceResponse;
import io.github.trashemailtelegramconnector.config.TelegramConnectorConfig;
import io.github.trashemailtelegramconnector.exceptions.EmailAliasNotCreatedExecption;
import io.github.trashemailtelegramconnector.models.FreeUserId;
import io.github.trashemailtelegramconnector.models.UsedUserId;
import io.github.trashemailtelegramconnector.models.User;
import io.github.trashemailtelegramconnector.repository.FreeUserIdRepository;
import io.github.trashemailtelegramconnector.repository.UsedUserIdRepository;
import io.github.trashemailtelegramconnector.repository.UserRepository;
import io.github.trashemailtelegramconnector.telegram.TelegramMessageResponse;
import io.github.trashemailtelegramconnector.telegram.messageEntities.InlineKeyboardButton;
import io.github.trashemailtelegramconnector.telegram.messageEntities.InlineKeyboardMarkup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramRequestHandler {

    @Autowired
    TelegramConnectorConfig telegramConnectorConfig;
    @Autowired
    TrashEmailServiceInteraction trashEmailServiceInteraction;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UsedUserIdRepository usedUserIdRepository;
    @Autowired
    FreeUserIdRepository freeUserIdRepository;

    private static final Logger log = LoggerFactory.getLogger(
            TelegramRequestHandler.class);

    public String createEmail(User user)
    throws HttpClientErrorException, EmailAliasNotCreatedExecption {
        log.info(user.toString());
        TrashEmailServiceRequest trashEmailServiceRequest = new TrashEmailServiceRequest(user);
        trashEmailServiceRequest.setSource(telegramConnectorConfig.getSource());
        trashEmailServiceRequest.setDestination(telegramConnectorConfig.getSource() + "/telegram/sendMessage");
        trashEmailServiceRequest.setRequestPath("/create");

        TrashEmailServiceResponse trashEmailServiceResponse =
                trashEmailServiceInteraction.processRequest(trashEmailServiceRequest);

        if(trashEmailServiceResponse.getCreated() != null){
            return "CREATED";
        }
        else
            return null;
    }
    public String deleteEmail(User user)
    throws HttpClientErrorException, EmailAliasNotCreatedExecption {
        TrashEmailServiceRequest trashEmailServiceRequest = new TrashEmailServiceRequest(user);
        trashEmailServiceRequest.setSource(telegramConnectorConfig.getSource());
        trashEmailServiceRequest.setDestination(telegramConnectorConfig.getSource() + "/telegram/sendMessage");
        trashEmailServiceRequest.setRequestPath("/delete");

        TrashEmailServiceResponse tsr = trashEmailServiceInteraction.processRequest(trashEmailServiceRequest);
        if(tsr.getIsDeleted()){

            user.setIsActive(false);
            userRepository.save(user);

            return "Email Id *deleted* and added to open pool.";
        }
        return "No mail ID on the server was identified with" +
                user.getEmailId();
    }

    public TelegramMessageResponse handleRequest(long chatId, String text) {

        try{
            String []strings = text.split("\\s+");

            String command 	= null;
            String argument = null;
            String responseText = "";

            if(strings.length >= 1)
                command = strings[0];
            if(strings.length >= 2)
                argument = strings[1];

            switch(command){
                case "/start":
                    responseText =  "Thanks for joining ...\n" +
                            "I am a disposable bot email id generator that can:\n" +
                            "1. Quickly generate a disposable email for you," +
                            " no email management hassle \uD83D\uDE0B\t\n" +
                            "2. Quickly delete those emailIds\n" +
                            "3. Since I own, " +
                            "*trashemail.in*, *humblemail.com* & *thromail.com* " +
                            "your emailId will belong these domains\n" +
                            "4. I don't read anything, I respect your privacy." +
                            "Any doubt? Audit my source code." +
                            "\n\n" +
                            "For now to get started try\n" +
                            "* /create <user>"+"\n" +
                            "* /delete\n" +
                            "* /help\n" +
                            "* /list\\_ids\n";

                    return new TelegramMessageResponse(
                            chatId,
                            responseText
                    );
                case "/generate":
                    String generate_response = "";
                    if(usedUserIdRepository.findByChatIdAndIsActiveTrue(chatId).size()>=2)
                        generate_response = "You already have two random emails " +
                                "ids";
                    else {
                        FreeUserId freeUserId = freeUserIdRepository.findTopByOrderByIdAsc();

                        log.info(freeUserId.getUserId());

                        User user = new User();
                        user.setIsActive(true);
                        user.setChatId(chatId);
                        user.setEmailId(freeUserId.getUserId());

                        try {
                            generate_response = this.createEmail(user);
                        }
                        catch (EmailAliasNotCreatedExecption emailAliasNotCreatedExecption) {
                            log.error("Exception " + emailAliasNotCreatedExecption.getMessage());
                            responseText = "Email address is already taken." +
                                    "\n" +
                                    "Please try something else.";
                            return new TelegramMessageResponse(
                                    chatId,
                                    responseText
                            );

                        }
                        catch (HttpClientErrorException httpClientErrorException) {

                            log.error("Exception " +
                                              httpClientErrorException.getMessage());
                            responseText = "Email address is already taken." +
                                    "\nPlease try something else.";
                            return new TelegramMessageResponse(
                                    chatId,
                                    responseText
                            );
                        }

                        if (generate_response != null) {
                        /*
                        Perform this if the user id is created on the server
                        */
                            freeUserIdRepository.delete(freeUserId);

                            UsedUserId usedUserId = new UsedUserId();
                            usedUserId.setChatId(chatId);
                            usedUserId.setIsActive(true);
                            usedUserId.setUserId(freeUserId.getUserId());
                            usedUserIdRepository.save(usedUserId);

                            userRepository.save(user);

                            generate_response = "Email Id : " + user.getEmailId() +
                                    " created.\nThis will get disposed after 10 " +
                                    "mins";
                        }
                    }
                    return new TelegramMessageResponse(
                            chatId,
                            generate_response);

                case "/create":
                    if(userRepository.findByChatIdAndIsActiveTrue(chatId).size()
                            >= telegramConnectorConfig.getMaxEmailsPerUser()) {
                        responseText =  "Only " +
                                telegramConnectorConfig.getMaxEmailsPerUser() + " " +
                                "email Ids are allowed per-user\n" +
                                "You can get the list of all the email ids @ \n" +
                                "/list\\_ids";
                        return new TelegramMessageResponse(
                                chatId,
                                responseText
                        );
                    }

                    else{
                        if(argument != null) {
                                /*
                                User entered something like : /create username.
                                Now offer him interactive response.
                                */
                            String emailRegex = "^[A-Za-z0-9._%+-]+@(" +
                                    String.join("|", telegramConnectorConfig.getHosts()) +")$";

                            Pattern pattern = Pattern.compile(emailRegex);
                            Matcher matcher = pattern.matcher(argument);

                            // A valid domain email is inputted by the user
                            if (matcher.matches()) {
                                String emailId = argument;
                                if(userRepository.existsByEmailIdAndIsActiveTrue(
                                        emailId)){
                                    // Email ID Already taken
                                    responseText = "Email ID *" + argument + "* " +
                                            "is already taken, " +
                                            "please try some other email id.";
                                    return new TelegramMessageResponse(
                                            chatId,
                                            responseText
                                    );
                                }

                                User user = new User(chatId, emailId);

                                String response = null;
                                try{
                                    response = this.createEmail(user);
                                }catch (EmailAliasNotCreatedExecption emailAliasNotCreatedExecption){
                                    log.error("Exception " + emailAliasNotCreatedExecption.getMessage());
                                    responseText = "Email address is already taken.\nPlease try something else.";
                                    return new TelegramMessageResponse(
                                            chatId,
                                            responseText
                                    );

                                }catch (HttpClientErrorException httpClientErrorException){

                                    log.error("Exception " + httpClientErrorException.getMessage());
                                    responseText =  "Email address is already taken.\nPlease try something else.";
                                    return new TelegramMessageResponse(
                                            chatId,
                                            responseText
                                    );
                                }

                                if(response!=null) {
                                    // Check if the same user has taken the same
                                    // email before, then it would exist in DB so
                                    // just set isActive to true.
                                    User existingUser = userRepository.findByEmailIdAndChatId(emailId, chatId);
                                    if(existingUser == null)
                                        userRepository.save(user);
                                    else{
                                        existingUser.setIsActive(true);
                                        userRepository.save(existingUser);
                                    }
                                    responseText =  "Email successfully created" +
                                            " - " +
                                            "" +
                                            "*"+argument+"*" +
                                            "\n See all your email ids @ " +
                                            "/list\\_ids";

                                    return new TelegramMessageResponse(
                                            chatId,
                                            responseText
                                    );
                                }

                                responseText = "Something bad just happened with " +
                                        "me." +
                                        "Stay back till I get fixed.";
                                return new TelegramMessageResponse(
                                        chatId,
                                        responseText
                                );
                            }
                            else if(!matcher.matches()){

                                if (argument.contains("@") ||
                                        argument.contains("-")) {
                                    responseText = "EmailId contains illegal chars.";
                                    return new TelegramMessageResponse(
                                            chatId,
                                            responseText
                                    );
                                }

                                responseText = "Pick a domain for the emailId: " +
                                        "*"+argument+"*";
                                List<String> emailsHosts = telegramConnectorConfig.getHosts();

                                int buttonPerRow = 2;
                                int buttonsRow = (int) Math.ceil(
                                        (double)emailsHosts.size()/buttonPerRow);

                                List<List<InlineKeyboardButton>> buttonList =
                                        new ArrayList<>(buttonsRow);

                                for(int i=0;i<buttonsRow;++i)
                                    buttonList.add(new ArrayList<>());

                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();


                                for(int i=0; i < emailsHosts.size(); ++i){
                                    InlineKeyboardButton btn = new InlineKeyboardButton();
                                    btn.setText(emailsHosts.get(i));
                                    btn.setCallback_data(
                                            "/create "
                                            + argument
                                            + "@"+
                                            emailsHosts.get(i));

                                    buttonList.get(i / buttonPerRow).add(btn);
                                }

                                inlineKeyboardMarkup.setInlineKeyboardButtonList(
                                        buttonList);

                                return new TelegramMessageResponse(
                                        chatId,
                                        responseText,
                                        inlineKeyboardMarkup
                                );

                            }
                        }
                        else if(argument == null) {
                            // Setup interactive with inline keyboard messages.
                            responseText = "Please use command like /create username";
                            return new TelegramMessageResponse(
                                    chatId,
                                    responseText
                            );
                        }

                        responseText = "Email id should be of the form: `*@("+
                                String.join("|", telegramConnectorConfig.getHosts())+")`";
                        return new TelegramMessageResponse(
                                chatId,
                                responseText
                        );
                    }

                case "/help":
                    responseText = "Create disposable email addresses to " +
                            "protect you against spam and newsletters." +
                            "E-Mail forwarding made easy.\n" +
                            "I am open source and runs on open source services.\n" +
                            "Currently, I support:\n" +
                            "/create - That can get you one(or more) " +
                            "custom disposable emails.\n" +
                            "/delete - If you are getting spams for any mail id, " +
                            "just delete it because it is disposable.\n" +
                            "/list\\_ids - Get the list of all the email Ids "+
                            "that belongs to you.\n" +
                            "/help - this help message.\n";
                    return new TelegramMessageResponse(
                            chatId,
                            responseText
                    );

                case "/list_ids":
                    String response = "Currently, you have these email ids with you. \n";
                    List<User> allEmailsWithUser = userRepository.findByChatIdAndIsActiveTrue(chatId);

                    for(User emailWithUser: allEmailsWithUser){
                        response += emailWithUser + "\n";
                    }

                    return new TelegramMessageResponse(chatId, response);

                case "/delete":
                    if(argument == null){
                        responseText = "Pick an email to delete.";

                        List<User> emailsWithUser = userRepository.findByChatIdAndIsActiveTrue(chatId);

                        int buttonPerRow = 1;
                        int buttonsRow = (int) Math.ceil((double)emailsWithUser.size()/buttonPerRow);

                        List<List<InlineKeyboardButton>> buttonList = new ArrayList<>(buttonsRow);

                        for(int i=0;i<buttonsRow;++i)
                            buttonList.add(new ArrayList<>());

                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                        for(int i=0; i < emailsWithUser.size(); ++i){
                            InlineKeyboardButton btn = new InlineKeyboardButton();
                            btn.setText(emailsWithUser.get(i).toString());
                            btn.setCallback_data("/delete " + emailsWithUser.get(i).toString());
                            buttonList.get(i / buttonPerRow).add(btn);
                        }

                        inlineKeyboardMarkup.setInlineKeyboardButtonList(buttonList);

                        return new TelegramMessageResponse(
                                chatId,
                                responseText,
                                inlineKeyboardMarkup
                        );
                    }
                    else {
                        String emailRegex = "^[A-Za-z0-9._%+-]+@(" + String.join("|", telegramConnectorConfig.getHosts()) + ")$";

                        Pattern pattern = Pattern.compile(emailRegex);
                        Matcher matcher = pattern.matcher(argument);

                        // A valid domain email is inputted by the user
                        if (matcher.matches()) {
                            String emailId = argument;
                            User user = userRepository.findByEmailIdAndIsActiveTrue(
                                    emailId);
                            // user should only delete email owned by user.
                            if (userRepository.existsByEmailIdAndIsActiveTrue(emailId)) {
                                if (((Long) chatId).equals(user.getChatId())) {
                                    responseText = this.deleteEmail(user);
                                    return new TelegramMessageResponse(
                                            chatId,
                                            responseText
                                    );
                                }
                            } else {
                                responseText = "*Email not registered " +
                                        "to this user ..*";
                                return new TelegramMessageResponse(
                                        chatId,
                                        responseText
                                );
                            }

                        } else {
                            if (argument.isEmpty()) {
                                responseText = "Please use command like " +
                                        "/delete <custom_name>@" +
                                        "" + String.join(
                                        "|",
                                        telegramConnectorConfig.getHosts());
                                return new TelegramMessageResponse(
                                        chatId,
                                        responseText
                                );
                            }
                            responseText = "Email id should be of the form: `*@(" +
                                    String.join(
                                            "|",
                                            telegramConnectorConfig.getHosts()) + ")`";
                            return new TelegramMessageResponse(
                                    chatId,
                                    responseText
                            );
                        }
                    }

                case "/follow":
                    String follow_response = "Follow me on " +
                            "[Twitter](https://twitter.com/sehgal_rohit)";

                    return new TelegramMessageResponse(
                            chatId,
                            follow_response
                    );

                case "/sponsor":
                    String sponsor_response = "Average running cost of this " +
                            "per-month is $30, if you like the idea and would " +
                            "like to sponsor then you can buy me a " +
                            "coffee: https://www.buymeacoffee.com/r0hi7";
                    return new TelegramMessageResponse(
                            chatId,
                            sponsor_response
                    );

                case "/like":
                    String like_response = "*If* you like the idea and this " +
                            "product, do drop a star at github repo (where my " +
                            "hear and soul lives).\n Every star " +
                            "really " +
                            "motivates me :)\n"+
                            "https://github.com/TrashEmail/TrashEmail";
                    return new TelegramMessageResponse(
                            chatId,
                            like_response
                    );

                case "/source_code":
                    String source_response = " GitHub repo (where my " +
                            "heart and soul lives).\n"+
                            "https://github.com/TrashEmail/TrashEmail";
                    return new TelegramMessageResponse(
                            chatId,
                            source_response
                    );
                default:
                    responseText = "I don't understand that ...\n " +
                            "I only understand few commands.\n" +
                            "1. /create <user>\n" +
                            "2. /delete\n" +
                            "3. /help\n" +
                            "4. /list\\_id\n"+
                            "5. /follow\n" +
                            "6. /like\n"+
                            "7. /sponsor";

                    return new TelegramMessageResponse(
                            chatId,
                            responseText
                    );
            }
        }catch (Exception e){
            String exception_response = "Unable to perform this action right now, Please connect later !!";
            log.error(String.format("Unable to handle request %s", e.getMessage()));
            return new TelegramMessageResponse(chatId, exception_response);
        }
    }

}

