package code.adagedo.smsnotificationservice.dto.request;

import java.util.List;

public record SmsMessage(
        List<Destination> destinations,
        String from,
        Content content
) {
}
