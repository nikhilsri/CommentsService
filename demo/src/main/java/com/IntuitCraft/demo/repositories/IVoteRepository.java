package com.IntuitCraft.demo.repositories;

import com.IntuitCraft.demo.Entities.Dislikes;
import com.IntuitCraft.demo.Entities.Likes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface IVoteRepository {

    void saveLikes(Likes like);
    void saveDisLikes(Dislikes dislikes) ;
    Likes findLikesByCommentIdAndUserId(Long commentId, String userId);
    Dislikes findDislikesByCommentIDAndUserId(Long commentId, String userId);

    void removeDislikeByUserAndComment(String userId, Long commentId);
    void removeLikeByUserAndComment(String userId, Long commentId);

    Page<String> findUserIdsForVoteByCommentId(Long commentId, String tableName, Pageable pageable);

}
