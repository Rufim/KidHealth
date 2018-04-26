package ru.constant.kidhealth.domain.event;


import ru.kazantsev.template.domain.event.ResponseEvent;

/**
 * Created by Dmitry on 01.08.2016.
 */
public abstract class ErrorEvent<T> extends ResponseEvent<T> {
    public final String error;

    public ErrorEvent(Status status, T response, String error) {
        super(status, response);
        this.error = error;
    }
}
