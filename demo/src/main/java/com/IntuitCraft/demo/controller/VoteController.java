package com.IntuitCraft.demo.controller;

import com.IntuitCraft.demo.service.VoteService;
import com.IntuitCraft.demo.utils.VotesConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vote")
public class VoteController {


    @Autowired
    VoteService voteService;

    @PutMapping(path = "/{comment_id}/{voteType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Like comments",tags = "Votes")
    public ResponseEntity<?> likeComment(@PathVariable("comment_id") Long commentId, @Parameter(description = "give like or dislike from UI") @PathVariable("voteType") String voteType, @RequestBody String userId) {
        boolean upvoteSuccess = false;
        if(VotesConstants.LIKE.equalsIgnoreCase(voteType))
            upvoteSuccess=voteService.likeComment(commentId,userId);
        else
            upvoteSuccess=voteService.dislikeComment(commentId,userId);
        if (upvoteSuccess) {
            return ResponseEntity.ok().body(new String ("Successfully "+voteType+"d the comment"));
        } else {
            return ResponseEntity.badRequest().body(new String("Failed to "+voteType+"d  comment"));
        }
    }
}
