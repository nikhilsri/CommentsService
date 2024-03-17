package com.IntuitCraft.demo.exceptions;

import org.springframework.http.HttpStatus;

public class PostsException extends GlobalException{
    protected PostsException(HttpStatus statusCode, String errorCode, String errorDescription) {
        super(statusCode, errorCode, errorDescription);
    }

    public static final CommentsException POST_NOT_ADDED = new CommentsException(HttpStatus.INTERNAL_SERVER_ERROR, "POST-1001", "Post.not.added");


}
