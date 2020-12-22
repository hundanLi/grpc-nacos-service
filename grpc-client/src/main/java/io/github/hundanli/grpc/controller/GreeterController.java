package io.github.hundanli.grpc.controller;

import io.github.hundanli.grpc.greeter.GreeterRequest;
import io.github.hundanli.grpc.greeter.GreeterServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hundanli
 * @version 1.0.0
 * @date 2020/12/21 20:15
 */
@RestController
public class GreeterController {

    /**
     * {link @GrpcClient}的value值是grpc-server注册到nacos上的服务名称
     */
    @GrpcClient("greeter-grpc-service")
    GreeterServiceGrpc.GreeterServiceBlockingStub stub;

    @GetMapping("/greeter/{name}")
    public String greeter(@PathVariable String name) {
        return stub.sayHello(GreeterRequest.newBuilder().setName(name).build()).getMessage();
    }

}
