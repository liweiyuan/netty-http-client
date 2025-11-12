# Netty HTTP Client

一个基于 Netty 构建的高性能、异步 HTTP 客户端库。

## 特性

- 基于 Netty 4.1.x 构建
- 异步 API 设计，支持 CompletableFuture
- 连接池管理，提高性能
- 支持 HTTP/1.1 协议
- 超时控制（连接超时、读取超时、写入超时、请求超时）
- 自动资源管理，防止内存泄漏
- 支持 GET、POST、PUT、DELETE 等 HTTP 方法

## 依赖

- Java 8+
- Netty 4.1.128.Final
- JUnit 4.13.2 (测试)

## 安装

### Maven

```xml
<dependency>
  <groupId>com.example</groupId>
  <artifactId>netty-http-client</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

### 构建

```bash
mvn clean install
```

## 使用示例

### 基本用法

```java
// 创建客户端
NettyHttpClient client = NettyHttpClient.builder()
    .config(HttpClientConfig.builder()
        .poolSize(10)
        .connectTimeoutMillis(5000)
        .build())
    .build();

// 发送 GET 请求
CompletableFuture<RichHttpResponse> future = client.get("http://example.com");
RichHttpResponse response = future.join();

System.out.println("Status: " + response.getStatusCode());
System.out.println("Body: " + response.getBody());

// 关闭客户端
client.shutdown();
```

### 发送 POST 请求

```java
Map<String, String> headers = new HashMap<>();
headers.put("Content-Type", "application/json");

String requestBody = "{\"name\":\"example\"}";
CompletableFuture<RichHttpResponse> future = client.post(
    "http://example.com/api", 
    requestBody.getBytes(StandardCharsets.UTF_8), 
    headers
);

RichHttpResponse response = future.join();
```

## 核心组件

### NettyHttpClient
主要的 HTTP 客户端类，提供各种 HTTP 方法的接口。

### HttpClientConfig
客户端配置类，用于设置连接池大小、超时时间等参数。

### ConnectionPool
连接池管理器，负责管理 HTTP 连接的复用。

### RichHttpResponse
增强的 HTTP 响应类，提供更方便的响应数据访问方法。

## 设计特点

### 异步非阻塞
所有网络操作都是异步的，使用 CompletableFuture 作为返回值，避免阻塞线程。

### 连接池
内置连接池支持，可以有效复用连接，减少连接建立的开销。

### 资源管理
完善的资源管理机制，确保在各种情况下都能正确释放 Netty 的 ByteBuf 等资源。

### 超时控制
支持多种超时控制：
- 连接超时
- 读取超时
- 写入超时
- 请求总超时

## 测试

### 运行测试

```bash
mvn test
```

### 使用外部HTTP服务进行测试

项目支持使用外部HTTP服务进行测试。例如，你可以使用Python启动一个简单的HTTP服务器：

```bash
mkdir test-server
echo "Hello from Python Test Server" > test-server/index.html
cd test-server && python3 -m http.server 8080
```

然后运行针对外部服务的测试：

```bash
mvn test -Dtest=ExternalHttpServiceTest
```

## 许可证

MIT License