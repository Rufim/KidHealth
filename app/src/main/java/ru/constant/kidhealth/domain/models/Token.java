package ru.constant.kidhealth.domain.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import lombok.Data;

/**
 * Created by 0shad on 20.06.2016.
 */

@Data
public class Token implements Serializable {

    private String accessToken;
    private String refreshToken;
    private Date accessExpires;
    private Date refreshExpires;
    private User account;

    public boolean isValid() {
        return accessToken != null && refreshToken != null && !isAccessExpired() && !isRefreshExpired();
    }

    public boolean isAccessExpired() {
        return accessExpires == null || accessExpires.getTime() - currentTime() - 5000 < 0;
    }

    public boolean isRefreshExpired() {
        return refreshExpires == null || refreshExpires.getTime() - currentTime() - 5000 < 0;
    }

    private static long currentTime() {
        Calendar time = Calendar.getInstance();
        time.add(Calendar.MILLISECOND, -time.getTimeZone().getOffset(time.getTimeInMillis()));
        return time.getTime().getTime();
    }

}
