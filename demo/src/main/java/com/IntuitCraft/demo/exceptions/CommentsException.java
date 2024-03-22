package com.IntuitCraft.demo.exceptions;

import org.springframework.http.HttpStatus;

public class CommentsException extends GlobalException{
    protected CommentsException(HttpStatus statusCode, String errorCode, String errorDescription) {
        super(statusCode, errorCode, errorDescription);
    }

    public static final CommentsException COMMENT_NOT_FOUND = new CommentsException(HttpStatus.NOT_FOUND, "COMMENT-1001", "Comment.not.found.for.parameters");

    public static final CommentsException COMMENT_BAD_REQUEST = new CommentsException(HttpStatus.BAD_REQUEST, "COMMENT-1002", "Bad.request.found");

    public static final CommentsException UNABLE_TO_DELETE_COMMENT = new CommentsException(HttpStatus.BAD_REQUEST, "COMMENT-1003", "user.not.owner.of.comment");

}
