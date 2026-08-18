package com.example.Liderum;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=test_only_jwt_secret_not_for_runtime_32_bytes_minimum")
class LiderumApplicationTests {

	@Test
	void contextLoads() {
	}

}
