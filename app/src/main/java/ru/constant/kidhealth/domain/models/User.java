package ru.constant.kidhealth.domain.models;

import android.content.Context;

import org.joda.time.LocalDate;

import java.io.Serializable;

import lombok.Data;
import ru.constant.kidhealth.utils.AppUtils;

/**
 * Created by 0shad on 20.06.2016.
 */

@Data
public class User implements Serializable {

    private static long point = System.currentTimeMillis();

    private String id;
    private String name;
    private String surname;
    private String middleName;
    private LocalDate dateOfBirth;
    private Sex sex;
    private String phone;
    private boolean enabled;

    private String login;
    private String password;

    public static User getInstance(Context context) {
        return  AppUtils.getLastUser();
    }

    public enum Sex {
        MALE,
        FEMALE
    }
}
