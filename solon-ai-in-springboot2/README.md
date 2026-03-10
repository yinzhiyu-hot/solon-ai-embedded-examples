# solon-ai-in-springboot2 说明文档与使用指南

## 1. 项目定位

这是一个 **Spring Boot 2 + Solon AI + MCP** 的融合示例，目标是演示：

- 在 Spring Boot 容器中使用 Solon AI 的 `Chat / Tool / Skill / RAG / Agent`
- 在同一进程里发布 MCP Server（含自动端点和手动端点两种方式）
- Web API 与 MCP Tool 复用同一份业务代码

技术基线：

- Java: 8
- Spring Boot: 2.7.18
- Solon AI: 3.9.5
- 默认模型提供方: Ollama

关键依赖见 [pom.xml](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/pom.xml:1)。

---

## 2. 总体架构

### 2.1 运行时结构

- Spring Boot 主容器负责 Web 接口、Bean 管理、调度任务
- `McpServerConfig` 在启动时拉起 Solon 子应用（最小扫描模式）用于 MCP 协议处理
- `SolonServletFilter` 将 `/mcp/*` 请求桥接到 Solon 处理链

对应代码：

- 启动入口: [HelloApp.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/HelloApp.java:1)
- MCP 启动与桥接: [McpServerConfig.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/mcpserver/McpServerConfig.java:1)
- MCP 鉴权过滤器: [McpServerAuth.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/mcpserver/McpServerAuth.java:1)

### 2.2 业务能力分层

- LLM 层: `ChatModel`、Tool Call、Skill 路由
- RAG 层: Embedding + InMemoryRepository + 文本切分
- Agent 层: ReActAgent + InMemory Session
- MCP 层: Tool/Resource/Prompt 端点发布

---

## 3. 代码模块说明

### 3.1 启动与基础接口

- [HelloApp.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/HelloApp.java:1)

职责：

- Spring Boot 启动入口
- 开启定时任务（`@EnableScheduling`）
- 提供基础健康示例接口 `/`、`/hello2`

### 3.2 LLM 配置与调用

- 模型常量: [_Constants.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/llm/_Constants.java:1)
- ChatModel 配置: [ChatConfig.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/llm/ChatConfig.java:1)
- Chat API: [ChatController.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/llm/ChatController.java:1)

要点：

- `chatModel` 注入了默认工具（天气、计算器）
- `chatModelForSkill` 注入了 `WeatherSkill`
- 支持同步调用与 SSE 流式输出

### 3.3 RAG 与可用性保护

- RAG 配置: [RagConfig.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/llm/RagConfig.java:1)
- RAG API: [RagController.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/llm/RagController.java:1)
- RAG 健康监控: [RagHealthMonitor.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/llm/RagHealthMonitor.java:1)

当前策略（方案 A）：

- 启动时尝试初始化知识库
- 若 embedding 模型不可用，不阻断服务启动（降级为 `DEGRADED`）
- 提供 `/ops/rag/health` 查询 RAG 运行状态

### 3.4 Agent

- 配置: [AgentConfig.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/agent/AgentConfig.java:1)
- 接口: [AgentController.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/agent/AgentController.java:1)

要点：

- 使用 `ReActAgent`
- 会话存储为内存 Map（`sessionId -> AgentSession`）

### 3.5 MCP 服务

- 自动端点样例: [McpServerTool.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/mcpserver/tool/McpServerTool.java:1)
- 手动端点样例: [McpServerTool2.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/mcpserver/tool/McpServerTool2.java:1)
- Web 与 MCP 复用: [WebController.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/web2mcp/WebController.java:1)

当前端点：

- `/mcp/demo1/sse`（注解驱动自动发布）
- `/mcp/demo2/sse`（手工构建发布）
- `/mcp/web/`（WebController 同时作为 MCP 端点）

鉴权策略：

- 非 `/message` 的 `/mcp/*` 请求会走 `McpServerAuth`
- `user=no` 模拟 401 拒绝

---

## 4. HTTP 接口清单

### 4.1 基础

- `GET/POST /` 参数: `name`
- `GET/POST /hello2` 参数: `name`

### 4.2 Chat

- `POST /chat/call` 参数: `prompt`
- `POST /chat/call_skill` 参数: `query`
- `POST /chat/stream` 参数: `prompt`，返回 `text/event-stream`

### 4.3 RAG

- `POST /rag/demo` 参数: `prompt`
- `GET /ops/rag/health` 返回: `status/message/lastCheckAt/provider/model/apiUrl`

### 4.4 Agent

- `POST /agent/call` 参数: `sessionId`, `query`

### 4.5 Web2MCP 复用接口

- `GET/POST /web/api/getWeather` 参数: `city`

---

## 5. 配置说明

配置文件: [application.yml](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/resources/application.yml:1)

### 5.1 RAG 启动与健康检查

```yaml
rag:
  init:
    enabled: true
  health:
    enabled: true
    interval-ms: 60000
    initial-delay-ms: 30000
    check-text: "health-check"
```

参数含义：

- `rag.init.enabled`: 是否启动时灌入示例知识库
- `rag.health.enabled`: 是否开启 embedding 可用性探测
- `rag.health.interval-ms`: 周期探测间隔
- `rag.health.initial-delay-ms`: 首次周期探测延迟
- `rag.health.check-text`: embedding 探测文本

### 5.2 模型配置

常量在 [_Constants.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/main/java/webapp/llm/_Constants.java:1)

- Chat: `/api/chat` + `qwen2.5:1.5b`
- Embedding: `/api/embed` + `nomic-embed-text`

---

## 6. 快速启动指南

### 6.1 前置检查

1. 安装 JDK 8 与 Maven 3.8+
2. 安装并启动 Ollama
3. 准备模型：

```bash
ollama pull qwen2.5:1.5b
ollama pull nomic-embed-text
ollama list
```

### 6.2 编译

```bash
mvn -f solon-ai-in-springboot2/pom.xml clean package -DskipTests
```

### 6.3 启动

```bash
mvn -f solon-ai-in-springboot2/pom.xml spring-boot:run
```

或：

```bash
java -jar solon-ai-in-springboot2/target/solon-ai-in-springboot2-jar-with-dependencies.jar
```

---

## 7. 调用示例

### 7.1 Chat 同步

```bash
curl -X POST "http://127.0.0.1:8080/chat/call" -d "prompt=你好"
```

### 7.2 Chat 流式

```bash
curl -N -X POST "http://127.0.0.1:8080/chat/stream" -d "prompt=杭州今天天气怎么样"
```

### 7.3 RAG

```bash
curl -X POST "http://127.0.0.1:8080/rag/demo" -d "prompt=solon 是谁开发的？"
```

### 7.4 Agent

```bash
curl -X POST "http://127.0.0.1:8080/agent/call" -d "sessionId=s1" -d "query=hello"
```

### 7.5 RAG 健康状态

```bash
curl "http://127.0.0.1:8080/ops/rag/health"
```

---

## 8. MCP 使用指南

### 8.1 作为 MCP 客户端调用本服务

参考测试: [McpClientTest.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/test/java/client/McpClientTest.java:1)

核心配置：

```java
McpClientProvider toolProvider = McpClientProvider.builder()
    .channel(McpChannel.STREAMABLE)
    .apiUrl("http://localhost:8080/mcp/demo1/sse")
    .build();
```

### 8.2 鉴权验证

```java
.apiUrl("http://localhost:8080/mcp/demo1/sse?user=no")
```

预期：`401`。

### 8.3 STDIO 模式参考

参考: [McpStdioDemo.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/test/java/client/McpStdioDemo.java:1)

注意：STDIO 模式服务端不要打印控制台日志，避免污染协议流。

---

## 9. 测试与验证建议

现有测试：

- Chat: [LlmChatTest.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/test/java/client/LlmChatTest.java:1)
- RAG: [LlmRagTest.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/test/java/client/LlmRagTest.java:1)
- Agent: [AgentTest.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/test/java/client/AgentTest.java:1)
- MCP: [McpClientTest.java](F:/workspaces/ai_workspace/solon-ai-embedded-examples/solon-ai-in-springboot2/src/test/java/client/McpClientTest.java:1)

建议新增（生产前）：

- 模型不可用时 `rag/health=DEGRADED` 的集成测试
- `/chat/stream` 并发连接数压测（连接回收与超时）
- MCP 鉴权覆盖更多 header/token 场景

---

## 10. 常见故障与排查

### 10.1 `EmbeddingException: model "..." not found`

原因：embedding 模型未拉取或名称不一致。

排查：

```bash
ollama list
ollama pull nomic-embed-text
```

说明：当前策略下不会阻止应用启动，但 RAG 会降级。

### 10.2 `401`（MCP）

原因：`McpServerAuth` 命中鉴权拦截。

检查：

- URL 是否带了 `user=no`
- 鉴权规则是否按目标路径定制

### 10.3 SSE 前端无结束标志

`/chat/stream` 默认在末尾发送 `[DONE]`，前端应按此结束流处理。

---

## 11. 生产化建议（按优先级）

### P0

- 将 `_Constants` 改为外部化配置（`application.yml` + 环境变量），避免硬编码
- MCP 鉴权升级为 Token/JWT，不使用 query 参数
- 为 `/ops/rag/health` 接入监控告警（状态变化触发）

### P1

- `AgentSessionProvider` 从内存 Map 升级为 Redis（支持多实例）
- `InMemoryRepository` 迁移到持久化向量库（Milvus/pgvector/ES 向量）
- 对 `/chat/stream` 增加连接数、空闲时长、IP 级限流

### P2

- 增加统一 traceId、请求审计、敏感信息脱敏
- 构建灰度开关：是否开启 RAG 初始化、是否开启 MCP 端点

---

## 12. 设计取舍（Trade-off）

### 方案 A（当前实现）

- 优点：可用性高，模型故障不拖垮主服务
- 缺点：RAG 在故障期属于降级状态，结果质量不稳定

### 方案 B（严格失败）

- 优点：一致性高，服务启动即保证能力完整
- 缺点：模型依赖波动会放大为整体不可用

当前项目作为示例工程，采用方案 A 更合理；生产环境可按业务 SLA 选择 A/B 或混合策略。

---

## 13. 变更记录（与你当前分支相关）

- 编译参数已改为固定版本（避免 `invalid target release 1.8.0_281`）
- RAG 启动初始化改为可降级
- 默认 embedding 模型改为本机已存在的 `nomic-embed-text`
- 新增 RAG 健康探测与查询接口
