package ru.constant.kidhealth.domain.event;

import ru.constant.kidhealth.domain.models.DayAction;

public class UpdateAction extends MessageEvent<DayAction> {

    public static final int NEXT = 1;
    public static final int PREVIOUS = 2;

    int direction = 0;

    public UpdateAction(DayAction message) {
        super(message);
    }

    public UpdateAction(DayAction message, int direction) {
        super(message);
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }
}
