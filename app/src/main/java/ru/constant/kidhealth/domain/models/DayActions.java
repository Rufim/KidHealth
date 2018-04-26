package ru.constant.kidhealth.domain.models;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class DayActions {
    private Date date;
    private WeekDay weekDay;
    private List<DayAction> dayActions;
}
