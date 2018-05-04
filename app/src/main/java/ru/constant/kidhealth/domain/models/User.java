package ru.constant.kidhealth.domain.models;

import android.content.Context;

import java.io.Serializable;

import lombok.Data;
import ru.constant.kidhealth.utils.AppUtils;

/**
 * Created by 0shad on 20.06.2016.
 */

@Data
public class User implements Serializable {

    private static long point = System.currentTimeMillis();

    String id = "";
    String name = "";
    String birthday = "";
    String phone = "";
    String login = "";
    String gender = "";
    Integer height;
    Integer weight;
    String password = "";
    String photo_path = "";

    public static User getInstance(Context context) {
        return  AppUtils.getLastUser();
    }
}
