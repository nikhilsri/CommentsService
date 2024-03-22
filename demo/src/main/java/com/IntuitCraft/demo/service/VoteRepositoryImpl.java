package com.IntuitCraft.demo.service;

import com.IntuitCraft.demo.Entities.Dislikes;
import com.IntuitCraft.demo.Entities.Likes;
import com.IntuitCraft.demo.repositories.IVoteRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class VoteRepositoryImpl implements IVoteRepository {

    private final Jdbi jdbi;

    public VoteRepositoryImpl(@Value("${spring.datasource.url}")
                              String URL,@Value("${spring.datasource.password}")
                              String PASSWORD,@Value("${spring.datasource.username}")
                              String USERNAME) {
        this.jdbi = Jdbi.create(URL,USERNAME,PASSWORD);
    }

    @Override
    public void saveLikes(Likes like) {

        jdbi.useHandle(handle -> {
            handle.createUpdate("INSERT INTO likes (comment_id, user_id, post_id) VALUES (:commentId, :userId, :postId)")
                    .bind("commentId", like.getCommentId())
                    .bind("userId", like.getUserId())
                    .bind("postId", like.getPostId())
                    .execute();
        });
    }

    @Override
    public void removeDislikeByUserAndComment(String userId, Long commentId) {
        String sql = "DELETE FROM dislikes WHERE user_id = :userId AND comment_id = :commentId";

        jdbi.useHandle(handle -> handle.createUpdate(sql)
                .bind("userId", userId)
                .bind("commentId", commentId)
                .execute());
    }

    @Override
    public void removeLikeByUserAndComment(String userId, Long commentId) {
        String sql = "DELETE FROM likes WHERE user_id = :userId AND comment_id = :commentId";

        jdbi.useHandle(handle -> handle.createUpdate(sql)
                .bind("userId", userId)
                .bind("commentId", commentId)
                .execute());
    }

    @Override
    public Page<String> findUserIdsForVoteByCommentId(Long commentId, String tableName, Pageable pageable) {
        String sql="select user_id from " +tableName+ " where comment_id = :commentId order by timestamp desc";
        List<String> allUserIds = jdbi.withHandle(handle ->
                handle.createQuery(sql)
                        .bind("commentId", commentId)
                        .mapTo(String.class)
                        .list()
        );

        // Calculate pagination parameters
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int fromIndex = pageNumber * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, allUserIds.size());

        // Get the sublist for the current page
        List<String> userIDsForPage = allUserIds.subList(fromIndex, toIndex);

        // Create and return the Page object
        return new PageImpl<>(userIDsForPage, pageable, allUserIds.size());

    }

    @Override
    public void saveDisLikes(Dislikes dislikes) {

        jdbi.useHandle(handle -> {
            handle.createUpdate("INSERT INTO dislikes (comment_id, user_id, post_id) VALUES (:commentId, :userId, :postId)")
                    .bind("commentId", dislikes.getCommentId())
                    .bind("userId", dislikes.getUserId())
                    .bind("postId", dislikes.getPostId())
                    .execute();
        });
    }


    @Override
    public Likes findLikesByCommentIdAndUserId(Long commentId, String userId) {
        String sql = "SELECT id, post_id, user_id, comment_id FROM likes WHERE comment_id = :commentId and user_id= :userId";
        return jdbi.withHandle(handle ->
                handle.createQuery(sql)
                        .bind("commentId", commentId)
                        .bind("userId", userId)
                        .mapToBean(Likes.class)
                        .findFirst()
                        .orElse(null)
        );
    }

    @Override
    public Dislikes findDislikesByCommentIDAndUserId(Long commentId, String userId) {
        String sql = "SELECT id, post_id, user_id, comment_id FROM dislikes WHERE comment_id = :commentId and user_id= :userId";
        return jdbi.withHandle(handle ->
                handle.createQuery(sql)
                        .bind("commentId", commentId)
                        .bind("userId", userId)
                        .mapToBean(Dislikes.class)
                        .findFirst()
                        .orElse(null)
        );
    }
}
