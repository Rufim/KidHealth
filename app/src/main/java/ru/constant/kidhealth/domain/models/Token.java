package ru.constant.kidhealth.domain.models;

import com.auth0.android.jwt.JWT;

import java.io.Serializable;

import lombok.Data;
import ru.kazantsev.template.domain.Validatable;
import ru.kazantsev.template.util.TextUtils;

/**
 * Created by 0shad on 20.06.2016.
 */

@Data
public class Token implements Serializable, Validatable{

    private String accessToken;
    private String refreshToken;
    private User account;

    public boolean isValid() {
        return !isAccessExpired() || !isRefreshExpired();
    }

    public boolean isAccessExpired() {
        return TextUtils.isEmpty(accessToken) || new JWT(accessToken).isExpired(10);
    }

    public boolean isRefreshExpired() {
        return TextUtils.isEmpty(refreshToken) || new JWT(refreshToken).isExpired(10);
    }

}
