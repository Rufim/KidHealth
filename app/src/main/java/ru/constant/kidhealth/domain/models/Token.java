package ru.constant.kidhealth.domain.models;

import com.auth0.android.jwt.JWT;

import org.joda.time.DateTime;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by 0shad on 20.06.2016.
 */

@Data
public class Token implements Serializable {

    private String accessToken;
    private String refreshToken;
    private User account;

    public boolean isValid() {
        return accessToken != null && refreshToken != null && !isAccessExpired() && !isRefreshExpired();
    }

    public boolean isAccessExpired() {
        return accessToken == null || new JWT(accessToken).isExpired(10);
    }

    public boolean isRefreshExpired() {
        return refreshToken == null || new JWT(refreshToken).isExpired(10);
    }

}
