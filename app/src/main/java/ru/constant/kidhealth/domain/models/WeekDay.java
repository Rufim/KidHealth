package ru.constant.kidhealth.domain.models;

import java.util.Calendar;

public enum  WeekDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    public static WeekDay getCalendarDay(Calendar calendar) {
        int calDay = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if(calDay == -1) calDay = 7;
        return values()[calDay];
    }
}
