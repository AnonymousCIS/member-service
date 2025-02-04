package org.anonymous.member.exceptions;

import org.anonymous.global.exceptions.CommonException;
import org.springframework.http.HttpStatus;

public class TempTokenNotFoundException extends CommonException {
    public TempTokenNotFoundException() {
        super("NotFound.tempToken", HttpStatus.NOT_FOUND);
        setErrorCode(true);
    }
}
