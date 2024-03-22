package com.IntuitCraft.demo.exceptions;

import com.IntuitCraft.demo.utils.CommonResponse;
import com.IntuitCraft.demo.utils.CustomDataResponse;
import com.IntuitCraft.demo.utils.ErrorData;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.UUID;

/**
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger("GlobalExceptionHandler");
    public static final String IS_1001 = "IS-1001";
    public static final String IS_1002 = "IS-1002";
    public static final String IS_1003 = "IS-1003";

    @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class))})
    @ApiResponse(responseCode = "404", description = "Not Found", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class))})
    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class))})
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInteractionExceptions(Exception ex, HttpServletRequest request) {
        log.error("Error occured in request " + request.getRequestURI(), ex);
        if (ex instanceof CommentsException commentsException) {
            return ResponseEntity.status(commentsException.getStatusCode()).body(new CommonResponse<CustomDataResponse<String>>(commentsException.getStatusCode().value(), request.getMethod(), UUID.randomUUID().toString(), new CustomDataResponse<>(commentsException.getStatusCode().value(), new ErrorData(commentsException.getErrorCode(), commentsException.getMessage(), commentsException.getErrorDescription()), null)));
        } else if (ex instanceof PostsException possException) {
            return ResponseEntity.status(possException.getStatusCode()).body(new CommonResponse<CustomDataResponse<String>>(possException.getStatusCode().value(), request.getMethod(), UUID.randomUUID().toString(), new CustomDataResponse<>(possException.getStatusCode().value(), new ErrorData(possException.getErrorCode(), possException.getMessage(), possException.getErrorDescription()), null)));
        }else if (ex instanceof ConstraintViolationException) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(new CommonResponse<CustomDataResponse<String>>(status.value(), request.getMethod(), UUID.randomUUID().toString(), new CustomDataResponse<>(status.value(), new ErrorData(IS_1001, ex.getMessage(), status.getReasonPhrase()), null)));
        } else {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(new CommonResponse<CustomDataResponse<String>>(status.value(), request.getMethod(), UUID.randomUUID().toString(), new CustomDataResponse<>(status.value(), new ErrorData(IS_1002, ex.getMessage(), status.getReasonPhrase()), null)));
        }
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("Error occured in request " + ((ServletWebRequest) request).getRequest().getRequestURI(), ex);
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, RequestAttributes.SCOPE_REQUEST);
        }
        String httpMethod = HttpMethod.GET.name();
        if (request instanceof ServletWebRequest servletWebRequest && null != servletWebRequest.getHttpMethod()) {
            httpMethod = servletWebRequest.getHttpMethod().name();
        }
        return ResponseEntity.status(status).body(new CommonResponse<CustomDataResponse<String>>(status.value(), httpMethod, UUID.randomUUID().toString(), new CustomDataResponse<>(status.value(), new ErrorData(IS_1003, ex.getMessage(), status.getReasonPhrase()), null)));
    }
}
