gRPC集成Spring Boot和Nacos服务发现。



### 1.1 集成Spring Boot

根据[开源项目](https://github.com/yidongnan/grpc-spring-boot-starter)集成Spring Boot到spring-boot项目中。

#### 1.1.1 服务端

##### 1.添加依赖和protobuf插件

```xml
<dependencies>
    <!--grpc-server依赖-->
    <dependency>
        <groupId>net.devh</groupId>
        <artifactId>grpc-server-spring-boot-starter</artifactId>
        <version>2.10.1.RELEASE</version>
    </dependency>
</dependencies>

<build>
    <extensions>
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.6.2</version>
        </extension>
    </extensions>
    <plugins>

        <!--protobuf代码生成插件-->
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
                <protocArtifact>com.google.protobuf:protoc:3.12.0:exe:${os.detected.classifier}</protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.34.1:exe:${os.detected.classifier}</pluginArtifact>
                <!--设置grpc生成代码到指定路径-->
                <outputDirectory>${project.basedir}/src/main/gen</outputDirectory>
                <!--生成代码前是否清空目录-->
                <clearOutputDirectory>false</clearOutputDirectory>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- 设置多个源文件夹 -->
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>build-helper-maven-plugin</artifactId>
            <version>3.0.0</version>
            <executions>
                <!-- 添加主源码目录 -->
                <execution>
                    <id>add-source</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>add-source</goal>
                    </goals>
                    <configuration>
                        <sources>
                            <source>${project.basedir}/src/main/gen</source>
                            <source>${project.basedir}/src/main/java</source>
                        </sources>
                    </configuration>
                </execution>
            </executions>
        </plugin>

    </plugins>
</build>

```



##### 2.定义proto并生成代码

```protobuf
syntax = "proto3";


option java_multiple_files = true;
option java_package = "io.github.hundanli.grpc.greeter";
option java_outer_classname = "GreeterProto";
option objc_class_prefix = "GTR";

service GreeterService {
  rpc sayHello(GreeterRequest) returns (GreeterReply) {}
}

message GreeterRequest {
  string name = 1;
}

message GreeterReply {
  string message = 1;
}
```

运行mvn命令生成代码

```bash
mvn protobuf:compile-custom
mvn clean compile
```

##### 3.编写rpc服务

在服务端接口实现类上添加 `@GrpcService` 注解。

```java
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
```

默认rpc server监听9090端口，可以查看`net.devh.boot.grpc.server.config.GrpcServerProperties`类的属性进行修改。

#### 1.1.2 客户端

##### 1.添加依赖

```xml
<dependencies>
    <dependency>
        <groupId>net.devh</groupId>
        <artifactId>grpc-client-spring-boot-starter</artifactId>
        <version>2.10.1.RELEASE</version>
    </dependency>
</dependencies>
<!-- 插件略... -->
```

##### 2.定义proto生成代码

与服务端一致。

##### 3.使用rpc客户端

修改配置文件application.properties:

```properties
grpc.client.myServer.address=static://localhost:9090
# 关闭ssl/tls
grpc.client.myServer.negotiation-type=plaintext 
```

使用`@GrpcClient(value = "myServer")`将stub注入Bean中即可使用，其中value属性值与配置文件对应。

```java
@Service
public class GreeterClient {
    @GrpcClient(value = "myServer")
    private GreeterServiceGrpc.GreeterServiceBlockingStub stub;


    public void sayHello() {
        GreeterReply reply = stub.sayHello(GreeterRequest.newBuilder().setName("hundanli").build());
        System.out.println(reply.getMessage());
    }
}
```



### 1.2 集成Nacos注册中心

#### 1.2.1 服务端

##### 1.添加依赖

pom.xml：

```xml
<!--grpc-server依赖-->
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-server-spring-boot-starter</artifactId>
    <version>2.10.1.RELEASE</version>
</dependency>

<!--nacos discovery-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    <version>2.2.0.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

##### 2.编写配置

application.yml：

```yaml
spring:
  application:
    name: greeter-grpc-service # 在GrpcClient需要引用
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        enabled: true

grpc:
  server:
    port: 9090
```

##### 3.开启服务发现

在主启动类上添加注解`@EnableDiscoveryClient`：

```java
@EnableDiscoveryClient
@SpringBootApplication
public class GrpcServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrpcServerApplication.class, args);
	}

}
```

#### 1.2.2 客户端

##### 1.添加依赖

```xml
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-client-spring-boot-starter</artifactId>
    <version>2.10.1.RELEASE</version>
</dependency>
<!--nacos discovery-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    <version>2.2.0.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

##### 2.编写配置

```yaml
server:
  port: 7070

grpc:
  client:
    GLOBAL:
      negotiation-type: plaintext
      enable-keep-alive: true
      keep-alive-without-calls: true

spring:
  application:
    name: greeter-grpc-client
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        enabled: true
```

##### 3.开启服务发现

```java
@SpringBootApplication
@EnableDiscoveryClient
public class GrpcClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrpcClientApplication.class, args);
	}

}
```

##### 4.注入stub

```java
@RestController
public class GreeterController {

    @GrpcClient("greeter-grpc-service") // 与grpc服务名称一致
    GreeterServiceGrpc.GreeterServiceBlockingStub stub;

    @GetMapping("/greeter/{name}")
    public String greeter(@PathVariable String name) {
        return stub.sayHello(GreeterRequest.newBuilder().setName(name).build()).getMessage();
    }

}
```







