package com.IntuitCraft.demo;

import com.IntuitCraft.demo.repositories.ICommentRepository;
import com.IntuitCraft.demo.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private CommentService commentService;

	@MockBean
	private ICommentRepository ICommentRepository;

	@Test
	public void testGetFirstLevelComments() {

	}

}
