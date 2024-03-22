package com.IntuitCraft.demo.controller;

import com.IntuitCraft.demo.service.VoteService;
import com.IntuitCraft.demo.utils.CommonResponse;
import com.IntuitCraft.demo.utils.VotesConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vote")
public class VoteController {


    @Autowired
    VoteService voteService;

    @PutMapping(path = "/{comment_id}/{voteType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Like/Dislike comments",tags = "Votes")
    public ResponseEntity<?> likeComment(@PathVariable("comment_id") Long commentId, @Parameter(description = "true for like else dislike") @PathVariable("voteType") Boolean
            voteType, @RequestBody String userId) {
        boolean upvoteSuccess = false;
        String vote="";
        if(voteType) {
            upvoteSuccess = voteService.likeComment(commentId, userId);
            vote = "like";
        }
        else {
            upvoteSuccess = voteService.dislikeComment(commentId, userId);
            vote="dislike";
        }
        if (upvoteSuccess) {
            return ResponseEntity.ok().body(new String ("Successfully "+vote+"d the comment"));
        } else {
            return ResponseEntity.badRequest().body(new String("Failed to "+vote+"d  comment"));
        }
    }

    @GetMapping(path = "/{comment_id}/{voteType}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Users who Likes/Disliked a comment",tags = "Votes")
    public CommonResponse<Page<String>> getUserListPerVoteType(@PathVariable("comment_id") Long commentId, @Parameter(description = "true for like else dislike")
    @PathVariable("voteType") Boolean voteType,
                                                               @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int pageNumber) {
        return new CommonResponse<Page<String>>(HttpStatus.OK.value(),
                RequestMethod.GET.toString(), UUID.randomUUID().toString()
                ,voteType?voteService.getCommentLikesUserList(commentId,VotesConstants.LIKES,pageNumber)
                :voteService.getCommentLikesUserList(commentId,VotesConstants.DISLIKES,pageNumber)
        );
    }
}
