package com.IntuitCraft.demo.exceptions;

import org.springframework.http.HttpStatus;

public class CommentsException extends GlobalException{
    protected CommentsException(HttpStatus statusCode, String errorCode, String errorDescription) {
        super(statusCode, errorCode, errorDescription);
    }

    public static final CommentsException COMMENT_NOT_FOUND = new CommentsException(HttpStatus.NOT_FOUND, "COMMENT-1001", "Comment.not.found.for.parameters");

}
