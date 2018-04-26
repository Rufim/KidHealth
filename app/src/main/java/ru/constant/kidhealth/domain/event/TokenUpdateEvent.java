package ru.constant.kidhealth.domain.event;

import ru.constant.kidhealth.domain.models.Token;
import ru.kazantsev.template.domain.event.ResponseEvent;

/**
 * Created by 0shad on 01.03.2016.
 */
public class TokenUpdateEvent extends ResponseEvent<Token> {
    public TokenUpdateEvent(Status status, Token response) {
        super(status, response);
    }
}
