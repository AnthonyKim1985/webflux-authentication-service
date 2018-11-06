package com.example.auth.vo.response;

import com.example.auth.domain.constant.ResponseStatus;
import lombok.*;

/**
 * @author Anthony Jinhyuk Kim
 * @version 1.0.0
 * @since 2018-10-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(of = "token", callSuper = false)
public class AuthResponse extends BasicResponse {
    private String token;

    public AuthResponse(ResponseStatus responseStatus, String token) {
        super(responseStatus);
        this.token = token;
    }
}
