package com.IntuitCraft.demo.controller;

import com.IntuitCraft.demo.Entities.Comment;
import com.IntuitCraft.demo.beans.CommentsOutputBean;
import com.IntuitCraft.demo.exceptions.CommentsException;
import com.IntuitCraft.demo.service.CommentService;
import com.IntuitCraft.demo.utils.CommonResponse;
import com.IntuitCraft.demo.utils.CustomDataResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private CommentService commentService;

    @GetMapping(path={"/post/{postId}/comment"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get first level comments for a post",tags = "Comments")
    public ResponseEntity<Page<Comment>> getFirstLevelComments(@PathVariable Long postId, @Valid @RequestParam(defaultValue = "1") @Positive int pageSize) {
        Page<Comment> comments = commentService.getFirstLevelComments(postId, pageSize);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @GetMapping(path = {"/{commentId}/replies"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get first level replies for a comment",tags = "Comments")
    public ResponseEntity<Page<Comment>> getReplies(@PathVariable Long commentId, @Valid @RequestParam(defaultValue = "1") @Positive int pageSize) {
        Page<Comment> replies = commentService.getReplies(commentId, pageSize);
        return new ResponseEntity<>(replies,HttpStatus.OK);
    }

    @PostMapping(path = {"/post/{postId}/comment"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add a comment to a post",tags = "Comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId, @RequestBody String content) {
        Comment comment = commentService.addComment(postId, content);
        return ResponseEntity.ok(comment);
    }

    @PostMapping(path = {"/comment/{commentId}/reply"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add a reply to a comment",tags = "Comments")
    public CommonResponse<CustomDataResponse<CommentsOutputBean>> addReply(@PathVariable Long commentId, @RequestBody String content) throws CommentsException {
        return new CommonResponse<CustomDataResponse<CommentsOutputBean>>(HttpStatus.OK.value(),
                RequestMethod.POST.toString(), UUID.randomUUID().toString(),
                new CustomDataResponse<CommentsOutputBean>(HttpStatus.OK.value(),
                        null, commentService.addReply(commentId, content)));
    }

}
