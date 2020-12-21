package io.github.hundanli.grpc.greeter;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

/**
 * @author hundanli
 * @version 1.0.0
 * @date 2020/12/21 12:43
 */
@Service
public class GreeterClient {
    @GrpcClient(value = "myServer")
    private GreeterServiceGrpc.GreeterServiceBlockingStub stub;


    public void sayHello() {
        GreeterReply reply = stub.sayHello(GreeterRequest.newBuilder().setName("hundanli").build());
        System.out.println(reply.getMessage());
    }
}
