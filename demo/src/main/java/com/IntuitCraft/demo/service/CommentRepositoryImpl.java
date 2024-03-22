package com.IntuitCraft.demo.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

import com.IntuitCraft.demo.Entities.Comment;
import com.IntuitCraft.demo.repositories.ICommentRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class CommentRepositoryImpl implements ICommentRepository {

    @Autowired
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    private final Jdbi jdbi;

    public CommentRepositoryImpl(@Value("${spring.datasource.url}")
                                 String URL,@Value("${spring.datasource.password}")
                                 String PASSWORD,@Value("${spring.datasource.username}")
                                 String USERNAME) {
        this.jdbi = Jdbi.create(URL,USERNAME,PASSWORD);
    }


    @Override
    public Page<Comment> findFirstLevelCommentsByPostId(Long postId, Pageable pageable) {
        String selectSql = "SELECT * FROM comments WHERE post_id = :postId AND parent_id IS NULL order by date_added desc LIMIT :limit OFFSET :offset";
        String countSql = "SELECT COUNT(*) FROM comments WHERE post_id = :postId AND parent_id IS NULL";

        List<Comment> comments = new ArrayList<>();
        long[] totalComments = new long[1];

        jdbi.useHandle(handle -> {
            // Execute the select query
            handle.createQuery(selectSql)
                    .bind("postId", postId)
                    .bind("limit", pageable.getPageSize())
                    .bind("offset", pageable.getOffset())
                    .mapToBean(Comment.class)
                    .forEach(comments::add);

            // Execute the count query
            totalComments[0] = handle.createQuery(countSql)
                    .bind("postId", postId)
                    .mapTo(Long.class)
                    .findOnly();
        });

        return new PageImpl<>(comments, pageable, totalComments[0]);
    }

    public long countFirstLevelCommentsByPostId(Long postId) {
        String jpql = "SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.parent.id IS NULL";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("postId", postId)
                .getSingleResult();
    }

    @Override
    public Comment save(Comment comment) {
        String sql = "INSERT INTO comments (post_id, parent_id, user_id, content, likes_count, dislikes_count) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, comment.getPostId());

            if(ObjectUtils.isNotEmpty(comment.getParentId())){
                Long parentId=comment.getParentId();
                preparedStatement.setLong(2, parentId);
            }else {
                preparedStatement.setNull(2, Types.BIGINT);
            }
            preparedStatement.setString(3, comment.getUserId()); // Change type to String
            preparedStatement.setString(4, comment.getContent());
            preparedStatement.setInt(5, comment.getLikesCount());
            preparedStatement.setInt(6, comment.getDislikesCount());
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Failed to retrieve generated keys for comment insertion.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception
        }
        return comment;
    }

    @Override
    public List<Comment> findByPostId(Long postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comments WHERE post_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, postId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Comment comment = new Comment();
                    comment.setId(resultSet.getLong("id"));
                    comment.setPostId(resultSet.getLong("post_id"));
                    comment.setParentId(resultSet.getLong("parent_id"));
                    comment.setUserId(resultSet.getString("user_id")); // Change type to String
                    comment.setContent(resultSet.getString("content"));
                    comment.setLikesCount(resultSet.getInt("likes_count"));
                    comment.setDislikesCount(resultSet.getInt("dislikes_count"));
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception
        }
        return comments;
    }

    @Override
    public void updateCommentLikes(Long commentId, int newLikes) {
        jdbi.useHandle(handle -> {
            handle.createUpdate("UPDATE comments SET likes_count = :newLikes WHERE id = :commentId")
                    .bind("newLikes", newLikes)
                    .bind("commentId", commentId)
                    .execute();
        });
    }

    @Override
    public void updateCommentDislikes(Long commentId, int newDislikes) {
        jdbi.useHandle(handle -> {
            handle.createUpdate("UPDATE comments SET dislikes_count = :newDislikes WHERE id = :commentId")
                    .bind("newDislikes", newDislikes)
                    .bind("commentId", commentId)
                    .execute();
        });
    }

    @Override
    public List<Comment> findByUserId(String userId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comments WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Comment comment = new Comment();
                    comment.setId(resultSet.getLong("id"));
                    comment.setPostId(resultSet.getLong("post_id"));
                    comment.setParentId(resultSet.getLong("parent_id"));
                    comment.setUserId(resultSet.getString("user_id")); // Change type to String
                    comment.setContent(resultSet.getString("content"));
                    comment.setLikesCount(resultSet.getInt("likes_count"));
                    comment.setDislikesCount(resultSet.getInt("dislikes_count"));
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception
        }
        return comments;
    }


    @Override
    public Page<Comment> findCommentsByParentIdPageable(Long parentId, Pageable pageable) {
        List<Comment> comments = new ArrayList<>();
        int totalComments = 0;

        String countSql = "SELECT COUNT(*) FROM comments WHERE parent_id = :parentId";
        String selectSql = "SELECT * FROM comments WHERE parent_id = :parentId order by date_added desc LIMIT :limit OFFSET :offset";

        try (Handle handle = jdbi.open()) {
            // Calculate total number of comments
            totalComments = handle.createQuery(countSql)
                    .bind("parentId", parentId)
                    .mapTo(Integer.class)
                    .one();

            // Retrieve comments for the specified page
            comments = handle.createQuery(selectSql)
                    .bind("parentId", parentId)
                    .bind("limit", pageable.getPageSize())
                    .bind("offset", pageable.getOffset())
                    .mapToBean(Comment.class)
                    .list();
        } catch (Exception ex) {
            // Handle exceptions
            ex.printStackTrace();
        }

        return new PageImpl<>(comments, pageable, totalComments);
    }


    @Override
    public void deleteCommentById(Long commentId){
        String sql = "DELETE FROM comments WHERE id = :commentId";

        try (Handle handle = jdbi.open()) {
            handle.createUpdate(sql)
                    .bind("commentId", commentId)
                    .execute();
        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace();
        }
    }

    @Override
    public Comment findCommentsByIdAndUserId(Long commentId, String userId) {
        String sql = "SELECT * FROM comments WHERE id = :commentId AND user_id = :userId";
        Comment comment = null;
        try (Handle handle = jdbi.open()) {
            comment= handle.createQuery(sql)
                    .bind("commentId", commentId)
                    .bind("userId", userId)
                    .mapToBean(Comment.class)
                    .findFirst().orElse(null);
        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace();

        }
        return comment;
    }

    @Override
    public Comment findByCommentId(Long commentId) {
        String sql = "SELECT * FROM comments WHERE id = :commentId LIMIT 1";

        try (Handle handle = jdbi.open()) {
            return handle.createQuery(sql)
                    .bind("commentId", commentId)
                    .mapToBean(Comment.class)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception
            return null;
        }
    }

    @Override
    public Comment findCommentsByParentId(Long parentId) {
        String sql = "SELECT * FROM comments WHERE id = :parentId LIMIT 1";
        return jdbi.withHandle(handle ->
                handle.createQuery(sql)
                        .bind("parentId", parentId)
                        .mapToBean(Comment.class)
                        .findFirst()
                        .orElse(null)
        );
    }

    @Override
    public void updateComment(Comment comment) {
        StringBuilder sql = new StringBuilder("UPDATE comments SET ");

        Long id = comment.getId();
        if (id == null) {
            throw new IllegalArgumentException("Comment id must be provided for update");
        }
        // Construct SET clause dynamically based on provided fields
        boolean first = true;
        if (comment.getPostId() != null) {
            sql.append(first ? "" : ", ").append("post_id = :postId");
            first = false;
        }
        if (comment.getUserId() != null) {
            sql.append(first ? "" : ", ").append("user_id = :userId");
            first = false;
        }
        if (comment.getParentId() != null) {
            sql.append(first ? "" : ", ").append("parent_id = :parent");
            first = false;
        }
        if (comment.getContent() != null) {
            sql.append(first ? "" : ", ").append("content = :content");
            first = false;
        }
        if (comment.getLikesCount() >= 0) {
            sql.append(first ? "" : ", ").append("likes_count = :likesCount");
            first = false;
        }
        if (comment.getDislikesCount() >= 0) {
            sql.append(first ? "" : ", ").append("dislikes_count = :dislikesCount");
            first = false;
        }

        sql.append(" WHERE id = :id");

        jdbi.useHandle(handle -> handle.createUpdate(sql.toString())
                .bindBean(comment)
                .execute());
    }
}
