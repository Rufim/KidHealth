package ru.constant.kidhealth.domain.event;


import ru.constant.kidhealth.domain.models.User;
import ru.kazantsev.template.domain.event.ResponseEvent;

/**
 * Created by Dmitry on 27.07.2016.
 */
public class UserUpdateEvent extends ResponseEvent<User> {
    public UserUpdateEvent(Status status, User response) {
        super(status, response);
    }
}
