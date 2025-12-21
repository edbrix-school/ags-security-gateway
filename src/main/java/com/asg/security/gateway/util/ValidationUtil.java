package com.asg.security.gateway.util;


import com.asg.security.gateway.exception.ValidationException;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    public static void validateRequiredField(Object value) {
        if (value !=null){
            if(StringUtils.isBlank(value.toString())) {
                throw new ValidationException("Value cannot be empty, required for this field");
            }
        }else{
            throw new ValidationException("Value cannot be null, required for this field");
        }

    }

    public static void validateEmailRequiredField(Object email) {
        validateRequiredField(email);

        if (!EMAIL_PATTERN.matcher(email.toString()).matches()) {
            throw new ValidationException("Email is not in proper format");
        }
    }
}