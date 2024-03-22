package com.IntuitCraft.demo.controller;

import com.IntuitCraft.demo.Entities.Comment;
import com.IntuitCraft.demo.beans.CommentsOutputBean;
import com.IntuitCraft.demo.beans.CommentsRequest;
import com.IntuitCraft.demo.exceptions.CommentsException;
import com.IntuitCraft.demo.service.CommentService;
import com.IntuitCraft.demo.utils.CommonResponse;
import com.IntuitCraft.demo.utils.CustomDataResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CommentController {
    @Autowired
    CommentService commentService;

    @GetMapping(path={"/post/{postId}/comment"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get first level comments for a post",tags = "Comments")
    public CommonResponse<Page<Comment>> getFirstLevelComments(@PathVariable Long postId, @Valid @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int pageNumber) {
        return new CommonResponse<Page<Comment>>(HttpStatus.OK.value(),
                RequestMethod.GET.toString(), UUID.randomUUID().toString(),
                commentService.getFirstLevelComments(postId, pageNumber)
                );
    }

    @GetMapping(path = {"/{commentId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get comment by Id",tags = "Comments")
    public CommonResponse<Comment> getCommentById(@PathVariable Long commentId) {
        return new CommonResponse<Comment>(HttpStatus.OK.value(),
                RequestMethod.GET.toString(), UUID.randomUUID().toString(),
                commentService.getCommentById(commentId)
        );
    }

    @DeleteMapping(path = {"/{commentId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete comment by Id",tags = "Comments")
    public CommonResponse<String> deleteCommentById(@PathVariable Long commentId,@RequestBody String userId) {
        return new CommonResponse<String>(HttpStatus.OK.value(),
                RequestMethod.POST.toString(), UUID.randomUUID().toString(),
                commentService.deleteCommentById(commentId,userId)
        );
    }

    @GetMapping(path = {"/{commentId}/replies"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get first level replies for a comment",tags = "Comments")
    public CommonResponse<Page<Comment>> getReplies(@PathVariable Long commentId, @Valid @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int pageNumber) {
        return new CommonResponse<Page<Comment>>(HttpStatus.OK.value(),
                RequestMethod.GET.toString(), UUID.randomUUID().toString(),
                commentService.getReplies(commentId, pageNumber)
                );
    }

    @PostMapping(path = {"/post/{postId}/comment"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add a comment to a post",tags = "Comments")
    public CommonResponse<Comment> addComment(@PathVariable Long postId, @RequestBody CommentsRequest content) {
        return new CommonResponse<Comment>(HttpStatus.OK.value(),
                RequestMethod.POST.toString(), UUID.randomUUID().toString(),
                commentService.addComment(postId, content)
                );
    }

    @PostMapping(path = {"/comment/{commentId}/reply"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add a reply to a comment",tags = "Comments")
    public CommonResponse<CommentsOutputBean> addReply(@PathVariable Long commentId, @RequestBody CommentsRequest content) throws CommentsException {
        return new CommonResponse<CommentsOutputBean>(HttpStatus.OK.value(),
                RequestMethod.POST.toString(), UUID.randomUUID().toString(),
                 commentService.addReply(commentId, content));
    }

}
