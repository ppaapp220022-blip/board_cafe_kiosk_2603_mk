package org.example.board_cafe_kiosk_2603.service.admin.sms;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiEmptyResponseException;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.exception.SolapiUnknownException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class SmsNurigoService {

    @Value("${sms.api.key}")
    private String apiKey;

    @Value("${sms.api.secret}")
    private String apiSecret;

    @Value("${sms.api.phone}")
    private String fromPhone;

    public void sendSms(String toPhoneNumber, String content) {
        // 매개변수를 DTO로 묶어서 유효성 검사
        DefaultMessageService messageService
                = SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);

        Message message = new Message();
        message.setFrom(fromPhone);
        message.setTo(toPhoneNumber);
        message.setText(content);


        try {
            messageService.send(message); // 발
        } catch (SolapiMessageNotReceivedException e) {
            // 발송에 실패한 메세지 확인
            log.error(e.getFailedMessageList());
            log.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (SolapiEmptyResponseException e) {
            throw new RuntimeException(e);
        } catch (SolapiUnknownException e) {
            throw new RuntimeException(e);
        }
    }

}
