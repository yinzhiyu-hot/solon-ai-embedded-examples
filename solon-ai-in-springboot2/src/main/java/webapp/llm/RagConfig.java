package webapp.llm;

import org.noear.solon.ai.embedding.EmbeddingModel;
import org.noear.solon.ai.embedding.EmbeddingException;
import org.noear.solon.ai.rag.Document;
import org.noear.solon.ai.rag.RepositoryStorable;
import org.noear.solon.ai.rag.repository.InMemoryRepository;
import org.noear.solon.ai.rag.splitter.RegexTextSplitter;
import org.noear.solon.ai.rag.splitter.SplitterPipeline;
import org.noear.solon.ai.rag.splitter.TokenSizeTextSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RagConfig {
    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    @Value("${rag.init.enabled:true}")
    private boolean ragInitEnabled;

    //适量嵌入模型
    @Bean
    public EmbeddingModel embeddingModel() {
        return EmbeddingModel.of(_Constants.embedding_apiUrl)
                .provider(_Constants.embedding_provider)
                .model(_Constants.embedding_model)
                .build();
    }

    //知识库
    @Bean
    public RepositoryStorable repository(EmbeddingModel embeddingModel) {
        return new InMemoryRepository(embeddingModel);
    }

    //知识库初始化
    @Bean
    public RagConfig initRepository(RepositoryStorable storable) throws Exception {
        if (!ragInitEnabled) {
            log.info("RAG repository bootstrap is disabled by config: rag.init.enabled=false");
            return this;
        }

        //示例文本
        String text = "Solon 框架由杭州无耳科技有限公司（下属 Noear 团队）开发并开源。是新一代，Java 企业级应用开发框架。从零开始构建，有自主的标准规范与开放生态。近16万行代码。\n" +
                "\n" +
                "追求： 快速、小巧、简洁\n" +
                "提倡： 克制、高效、开放";

        //示例切割
        List<Document> documents = new SplitterPipeline()
                .next(new RegexTextSplitter("\n"))
                .next(new TokenSizeTextSplitter(500))
                .split(text);

        try {
            //插入知识库
            storable.insert(documents);
            log.info("RAG repository bootstrap completed, inserted {} docs", documents.size());
        } catch (EmbeddingException e) {
            log.warn("RAG repository bootstrap skipped because embedding model is unavailable. model={}, provider={}, apiUrl={}, reason={}",
                    _Constants.embedding_model, _Constants.embedding_provider, _Constants.embedding_apiUrl, e.getMessage());
        }

        return this;
    }
}
