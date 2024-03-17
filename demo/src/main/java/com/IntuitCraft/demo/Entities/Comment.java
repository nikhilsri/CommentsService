package com.IntuitCraft.demo.Entities;

import lombok.Data;

import java.util.*;
import javax.persistence.*;

@Entity
@Data
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id")
    private Long postId;

    private String userId;


    @Column(name = "parent_id")
    private Long parent;

    private String content;
    private int likesCount;
    private int dislikesCount;

    // Getters and setters
}
