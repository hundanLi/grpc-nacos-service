package io.github.hundanli.grpc;

import io.github.hundanli.grpc.greeter.GreeterClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GrpcClientApplicationTests {

	@Autowired
	private GreeterClient client;

	@Test
	void test() {
		client.sayHello();
	}

}
