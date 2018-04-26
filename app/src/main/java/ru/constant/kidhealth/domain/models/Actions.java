package ru.constant.kidhealth.domain.models;

import java.util.HashMap;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
public class Actions {
    private List<DayAction> actions;
    private HashMap<WeekDay, List<DayAction>> actionMap;
    private String startDate;
    private String finishDate;
    private Integer period;
}
