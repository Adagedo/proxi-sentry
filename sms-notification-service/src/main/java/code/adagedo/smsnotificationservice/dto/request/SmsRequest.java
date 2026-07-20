package code.adagedo.smsnotificationservice.dto.request;

import java.util.List;

public record SmsRequest(
        List<SmsMessage> messages
) {
}
