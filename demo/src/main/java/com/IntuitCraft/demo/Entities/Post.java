package com.IntuitCraft.demo.Entities;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts")
public class Post implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String content;
    private String userId;
    private boolean isPostPopular;
    public Post(Long id) {
        this.id = id;
    }

    public Post(Long postId, String title, String content, String userId) {
        this.id=postId;
        this.title=title;
        this.content=content;
        this.userId=userId;
    }

    public Post(String title, String content, String userId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
    }
}

