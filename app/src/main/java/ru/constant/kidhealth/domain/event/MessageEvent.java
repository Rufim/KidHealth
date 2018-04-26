package ru.constant.kidhealth.domain.event;


import ru.kazantsev.template.domain.event.Event;

/**
 * Created by 0shad on 18.03.2016.
 */
public abstract class MessageEvent<M> implements Event {
    public final M message;

    protected MessageEvent(M message) {
        this.message = message;
    }
}
