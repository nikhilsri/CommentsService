package com.IntuitCraft.demo.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Objects;

public abstract class GlobalException extends RuntimeException {

    private static final long serialVersionUID = 007L;

    protected HttpStatus statusCode;
    protected String errorCode;
    protected String errorDescription;

    protected GlobalException(HttpStatus statusCode, String errorCode, String errorDescription) {
        super(errorDescription);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        GlobalException that = (GlobalException)obj;

        return (Objects.equals(errorDescription, that.errorDescription))
                && (Objects.equals(errorCode, that.errorCode))
                && statusCode == that.statusCode;
    }

    @Override
    public int hashCode() {
        return errorDescription != null ? errorDescription.hashCode() : 0;
    }

}

