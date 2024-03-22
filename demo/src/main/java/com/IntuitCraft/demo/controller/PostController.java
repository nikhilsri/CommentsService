package com.IntuitCraft.demo.controller;

import com.IntuitCraft.demo.Entities.Post;
import com.IntuitCraft.demo.beans.PostRequest;
import com.IntuitCraft.demo.service.PostService;
import com.IntuitCraft.demo.utils.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping(path = {"/posts/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add a new post",tags = "Posts",description = "Add a new post")
    public CommonResponse<Post> addPost(@RequestBody PostRequest postRequest) {
        return new CommonResponse<Post>(HttpStatus.OK.value(),
                RequestMethod.POST.toString(), UUID.randomUUID().toString()
                ,postService.addPost(postRequest));
    }

    @GetMapping(path = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "get the posts paginated by created date",tags = "Posts",description = "get the posts paginated by created date")
    public CommonResponse<Page<Post>> getPostsByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Date Format yyyy-MM-dd — for example, 2000-10-31")
            LocalDate date,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0")
            int pageNumber) {
        return new CommonResponse<Page<Post>>(HttpStatus.OK.value(),
                RequestMethod.GET.toString(), UUID.randomUUID().toString()
                ,postService.getPostsByDate(date, pageNumber));
    }
}
