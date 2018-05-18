package ru.constant.kidhealth.domain.event;

import ru.constant.kidhealth.domain.models.DayAction;
import ru.kazantsev.template.domain.event.Event;

public class UpdateAction extends MessageEvent<DayAction> {
    public UpdateAction(DayAction message) {
        super(message);
    }
}
