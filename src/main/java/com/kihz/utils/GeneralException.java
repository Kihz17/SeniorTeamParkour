package com.kihz.utils;

import com.kihz.Core;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException{
    private String alert;
    private Exception exception;

    public GeneralException(String alert) {
        this(null, alert);
    }

    public GeneralException(String alert, Object... args) {
        this(Utils.formatColor(alert, args));
    }

    public GeneralException(Exception e, String alert) {
        super(getMessage(alert, e), e);
        this.alert = alert;
        this.exception = e;

        try {
            Core.logInfo(alert);
        } catch (Exception ex) {
            System.out.println("WARNING: There was an error broadcasting the fact that there was an error.");
        }
    }

    /**
     * Get the displayed error message.
     * @param alert Alert for this exception.
     * @param e     Exception
     * @return message
     */
    private static String getMessage(String alert, Exception e) {
        return e != null && e.getLocalizedMessage() != null ?
                (e instanceof GeneralException ? e.getLocalizedMessage() : alert + " (" + e.getLocalizedMessage() + ")")
                : alert;
    }
}