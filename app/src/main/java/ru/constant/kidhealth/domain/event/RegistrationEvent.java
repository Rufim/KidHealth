package ru.constant.kidhealth.domain.event;

import ru.constant.kidhealth.domain.models.User;

/**
 * Created by Dmitry on 01.08.2016.
 */
public class RegistrationEvent extends ErrorEvent<User> {
    public RegistrationEvent(Status status, User response, String error) {
        super(status, response, error);
    }
}
