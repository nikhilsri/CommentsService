package com.IntuitCraft.demo.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;
import javax.persistence.*;

@Entity
@Data
@Table(name = "comments")
@AllArgsConstructor
@NoArgsConstructor
public class Comment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id")
    private Long postId;

    private String userId;


    @Column(name = "parent_id")
    private Long parentId;

    private String content;
    private int likesCount;
    private int dislikesCount;
    private Date dateAdded;
    private Long timeAddedEpoch;

    public Comment(long id, Long postId, String userId, String content) {

        this.id=id;
        this.postId=postId;
        this.userId=userId;
        this.content=content;
    }

    // Getters and setters
}
