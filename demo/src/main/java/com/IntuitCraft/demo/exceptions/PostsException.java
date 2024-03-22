package com.IntuitCraft.demo.exceptions;

import org.springframework.http.HttpStatus;

public class PostsException extends GlobalException{
    protected PostsException(HttpStatus statusCode, String errorCode, String errorDescription) {
        super(statusCode, errorCode, errorDescription);
    }

    public static final PostsException POST_NOT_ADDED = new PostsException(HttpStatus.INTERNAL_SERVER_ERROR, "POST-1001", "Post.not.added");

    public static final PostsException POST_NOT_FOUND = new PostsException(HttpStatus.NOT_FOUND, "POST-1002", "Post.not.found.for.parameters");

    public static final PostsException POST_BAD_REQUEST = new PostsException(HttpStatus.BAD_REQUEST, "POST-1003", "Bad.request.found");


}
