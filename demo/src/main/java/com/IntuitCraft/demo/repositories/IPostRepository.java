package com.IntuitCraft.demo.repositories;

import com.IntuitCraft.demo.Entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface IPostRepository {
        void save(Post post);
        Post findPostById(Long postId);

        Page<Post> getPostByDateAdded(LocalDate date, Pageable pageable);
}
