package ru.constant.kidhealth.domain.models;

public enum  ActionStatus {
    STARTED("Начато"),
    POSTPONED("Отложено"),
    STOPPED("Отменено"),
    FINISHED("Завершено");

    private String name;

    ActionStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
