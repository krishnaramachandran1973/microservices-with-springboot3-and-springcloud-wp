package com.itskool;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"eureka.client" +
		".enabled=false"})
class ProductServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
