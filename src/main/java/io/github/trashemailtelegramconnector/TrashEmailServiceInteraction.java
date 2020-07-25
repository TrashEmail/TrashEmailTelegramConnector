package io.github.trashemailtelegramconnector;

import io.github.trashemailtelegramconnector.DTO.TrashEmailServiceRequest;
import io.github.trashemailtelegramconnector.DTO.TrashEmailServiceResponse;
import io.github.trashemailtelegramconnector.config.TrashEmailConfig;
import io.github.trashemailtelegramconnector.exceptions.EmailAliasNotCreatedExecption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TrashEmailServiceInteraction {

    @Autowired
    TrashEmailConfig trashEmailConfig;

    @Autowired
    RestTemplate restTemplate;

    public TrashEmailServiceResponse processRequest(TrashEmailServiceRequest tsr)
    throws EmailAliasNotCreatedExecption {
        String targetURI = trashEmailConfig.getTrashEmailServiceUrl() + tsr.getRequestPath();
        ResponseEntity responseEntity = restTemplate.postForEntity(
                targetURI,
                tsr,
                TrashEmailServiceResponse.class
        );
        if(responseEntity.getStatusCode() == HttpStatus.OK || responseEntity.getStatusCode() == HttpStatus.CREATED){
            return (TrashEmailServiceResponse) responseEntity.getBody();
        }
        else{
            throw new EmailAliasNotCreatedExecption();
        }
    }
}
