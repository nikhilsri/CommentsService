package com.IntuitCraft.demo.controller;

import com.IntuitCraft.demo.Entities.Post;
import com.IntuitCraft.demo.beans.PostRequest;
import com.IntuitCraft.demo.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping(path = {"/posts/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add a new post",tags = "Posts",description = "Add a new post")
    public ResponseEntity<Post> addPost(@RequestBody PostRequest postRequest) {
        Post post = postService.addPost(postRequest);
        return ResponseEntity.ok(post);
    }

    @GetMapping(path = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "get the posts paginated by created date",tags = "Posts",description = "get the posts paginated by created date")
    public ResponseEntity<Page<Post>> getPostsByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") @Positive int pageSize) {
        Page<Post> posts = postService.getPostsByDate(date, pageSize);
        return ResponseEntity.ok(posts);
    }
}
