package webapp.llm;

import org.noear.solon.ai.embedding.EmbeddingException;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/ops/rag")
public class RagHealthMonitor {
    private static final Logger log = LoggerFactory.getLogger(RagHealthMonitor.class);

    private final EmbeddingModel embeddingModel;

    @Value("${rag.health.enabled:true}")
    private boolean healthEnabled;

    @Value("${rag.health.check-text:health-check}")
    private String checkText;

    private final AtomicReference<String> status = new AtomicReference<>("UNKNOWN");
    private volatile String message = "not checked yet";
    private volatile Instant lastCheckAt;

    public RagHealthMonitor(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @PostConstruct
    public void checkAtStartup() {
        if (!healthEnabled) {
            status.set("DISABLED");
            message = "rag.health.enabled=false";
            log.info("RAG health check is disabled by config");
            return;
        }

        doCheck();
    }

    @Scheduled(fixedDelayString = "${rag.health.interval-ms:60000}", initialDelayString = "${rag.health.initial-delay-ms:30000}")
    public void checkPeriodically() {
        if (!healthEnabled) {
            return;
        }

        doCheck();
    }

    private void doCheck() {
        try {
            embeddingModel.embed(checkText);
            lastCheckAt = Instant.now();
            message = "embedding model reachable";
            updateStatus("UP", "RAG embedding health is UP");
        } catch (EmbeddingException e) {
            lastCheckAt = Instant.now();
            message = e.getMessage();
            updateStatus("DEGRADED", "RAG embedding health is DEGRADED: " + e.getMessage());
        } catch (Exception e) {
            lastCheckAt = Instant.now();
            message = e.getClass().getSimpleName() + ": " + e.getMessage();
            updateStatus("DEGRADED", "RAG embedding health is DEGRADED: " + message);
        }
    }

    private void updateStatus(String newStatus, String logMessage) {
        String oldStatus = status.getAndSet(newStatus);
        if (!newStatus.equals(oldStatus)) {
            if ("UP".equals(newStatus)) {
                log.info("{} (from {})", logMessage, oldStatus);
            } else {
                log.warn("{} (from {})", logMessage, oldStatus);
            }
        }
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", status.get());
        result.put("message", message);
        result.put("lastCheckAt", lastCheckAt == null ? null : lastCheckAt.toString());
        result.put("provider", _Constants.embedding_provider);
        result.put("model", _Constants.embedding_model);
        result.put("apiUrl", _Constants.embedding_apiUrl);
        return result;
    }
}
