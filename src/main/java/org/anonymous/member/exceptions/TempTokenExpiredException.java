package org.anonymous.member.exceptions;

import org.anonymous.global.exceptions.CommonException;
import org.springframework.http.HttpStatus;

public class TempTokenExpiredException extends CommonException {

    public TempTokenExpiredException() {
        super("Expired.tempToken", HttpStatus.UNAUTHORIZED);
        setErrorCode(true);
    }
}
