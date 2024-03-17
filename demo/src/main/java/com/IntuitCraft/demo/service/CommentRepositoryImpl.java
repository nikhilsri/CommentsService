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
        List<Comment> comments = new ArrayList<>();
        long totalComments = 0;

        String selectSql = "SELECT * FROM comments WHERE post_id = ? AND parent_id IS NULL LIMIT ? OFFSET ?";
        String countSql = "SELECT COUNT(*) FROM comments WHERE post_id = ? AND parent_id IS NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {

            // Set parameters for the select query
            selectStmt.setLong(1, postId);
            selectStmt.setInt(2, pageable.getPageSize());
            selectStmt.setInt(3, (int) pageable.getOffset());

            // Execute the select query
            try (ResultSet rs = selectStmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setId(rs.getLong("id"));
                    comment.setPostId(rs.getLong("post_id"));
                    comment.setParent(rs.getLong("parent_id"));
                    comment.setUserId(rs.getString("user_id"));
                    comment.setContent(rs.getString("content"));
                    comments.add(comment);
                }
            }

            // Execute the count query
            countStmt.setLong(1, postId);
            try (ResultSet countRs = countStmt.executeQuery()) {
                if (countRs.next()) {
                    totalComments = countRs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            // Handle exceptions
            ex.printStackTrace();
        }

        return new PageImpl<>(comments, pageable, totalComments);
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

            if(ObjectUtils.isNotEmpty(comment.getParent())){
                Long parentId=comment.getParent();
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
                    comment.setParent(resultSet.getLong("parent_id"));
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
                    comment.setParent(resultSet.getLong("parent_id"));
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

        String countSql = "SELECT COUNT(*) FROM comments WHERE parent_id = ?";
        String selectSql = "SELECT * FROM comments WHERE parent_id = ? LIMIT ? OFFSET ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql);
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            // Calculate total number of comments
            countStmt.setLong(1, parentId);
            try (ResultSet countRs = countStmt.executeQuery()) {
                if (countRs.next()) {
                    totalComments +=1;
                }
            }

            // Retrieve comments for the specified page
            selectStmt.setLong(1, parentId);
            selectStmt.setInt(2, pageable.getPageSize());
            selectStmt.setInt(3, (int) pageable.getOffset());
            try (ResultSet rs = selectStmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setId(rs.getLong("id"));
                    comment.setPostId(rs.getLong("post_id"));
                    comment.setParent(rs.getLong("parent_id"));
                    comment.setUserId(rs.getString("user_id"));
                    comment.setContent(rs.getString("content"));
                    comments.add(comment);
                }
            }
        } catch (SQLException ex) {
            // Handle exceptions
            ex.printStackTrace();
        }

        return new PageImpl<>(comments, pageable, totalComments);
    }

    @Override
    public Comment findByCommentId(Long commentId) {
        String jpql = "SELECT c FROM Comment c WHERE c.id = :commentId";
        TypedQuery<Comment> query = entityManager.createQuery(jpql, Comment.class)
                .setParameter("commentId", commentId)
                .setMaxResults(1); // Limit the result to one
        return query.getResultList().isEmpty() ? null : query.getResultList().get(0);
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
        if (comment.getParent() != null) {
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
