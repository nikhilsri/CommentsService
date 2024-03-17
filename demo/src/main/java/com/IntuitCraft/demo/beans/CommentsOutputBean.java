package com.IntuitCraft.demo.beans;
import java.io.Serializable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


//Assumption: UI se get krte time comment parentId hamesha pass hogi meri get comment API me
//jb bhi view reply jaise kisi button pe click hoga to get comments ki api hit ho jaegi
//comment first time kisi post ka jb open hoga to on button click pe hoga and first 10 comments le aaunga if present
//based on date added desc, and cache bhi krna hoga kuch posts k comments ko initially
//fir hot posts ko cache me rakhenge
//User login kr k he ye sb comments n all kaam kr pa raha hai

//Read heavy 100:1, 100 reads 1 write
//I have made efforts to make it HA and eventual consistent
//Hot posts maine wo decide kri hai jinke posts log every now and then dekh rahe hain
//comments/likes cache kr raha hu hot posts k for 5 minutes,to jisko b hot post comments dikheneg
//5 min purane dikheneg fir jb cache evict hoga to dobara DB hit se lakr cache kr lenge.
//cache me sirf first level comments rkh rahe hain 10 comments(for demo)

//PSQL integrate krna hai or index banane hain

//Post pe jb first level comments honge to parent comment id null rahega,postId hogi,postId compulsory
//in every API for Comments controller

//Rate Limit,configured at CMS

//User can perform writes only if user is authenticated,like post & comment & reply


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommentsOutputBean implements Serializable {


        private Long id;
        private Long postId;
        private String userId;
        private Long parent;
        private String content;
        private int likesCount;
        private int dislikesCount;

    public CommentsOutputBean(Long id, Long postId, String content) {
        this.id=id;
        this.postId=postId;
        this.content=content;
    }

    // Getters and setters
}
