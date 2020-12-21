package io.github.hundanli.grpc.greeter;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * @author hundanli
 * @version 1.0.0
 * @date 2020/12/21 11:37
 */
@GrpcService
@Slf4j
public class GreeterService extends GreeterServiceGrpc.GreeterServiceImplBase {
    @Override
    public void sayHello(GreeterRequest request, StreamObserver<GreeterReply> responseObserver) {
        log.info("Received Greeter from {}", request.getName());
        responseObserver.onNext(GreeterReply.newBuilder().setMessage("Hi, " + request.getName()).build());
        responseObserver.onCompleted();
    }
}
