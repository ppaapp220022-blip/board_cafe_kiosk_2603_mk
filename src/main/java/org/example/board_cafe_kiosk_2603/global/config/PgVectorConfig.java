package org.example.board_cafe_kiosk_2603.global.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class PgVectorConfig {
    @Bean
    @ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true", matchIfMissing = true)
    @ConfigurationProperties("spring.datasource.pgvector")
    public DataSourceProperties pgVectorDataSourceProperties() {
        /* 데이터 소스 프로퍼티 빈 생성 -> PostgresSQL 전용 DB 연결 정보를 가져오기 위해 별도의 프로퍼티 설정
           URL, 계정, 비밀번호 등을 저장 */
        return new DataSourceProperties();
    }

    @Bean(name = "pgVectorDataSource")
    @ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true", matchIfMissing = true)
    public DataSource pgVectorDataSource() {
        /* 데이터베이스 연결 객체 생성 -> pgVectorDataSourceProperties에서 가져온 설정으로 PostgresSQL 연결 객체 생성 */
        return pgVectorDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "pgVectorJdbcTemplate")
    @ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true", matchIfMissing = true)
    public JdbcTemplate pgVectorJdbcTemplate(@Qualifier("pgVectorDataSource") DataSource pgVectorDataSource) {
        /* SQL 실행을 쉽게 해주는 Spring JDBC 객체 생성 -> pgVectorDataSource를 사용하는 JdbcTemplate 생성 */
        return new JdbcTemplate(pgVectorDataSource);
    }

    @Bean(name = "pgVectorTxManager")
    @ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true", matchIfMissing = true)
    public PlatformTransactionManager pgVectorTxManager(@Qualifier("pgVectorDataSource") DataSource pgVectorDataSource) {
        /* 트랜잭션 관리자 생성 -> pgVectorDataSource를 사용하는 트랜잭션 관리자 생성 */
        return new DataSourceTransactionManager(pgVectorDataSource);
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true", matchIfMissing = true)
    public VectorStore vectorStore(@Qualifier("pgVectorJdbcTemplate") JdbcTemplate pgVectorJdbcTemplate, EmbeddingModel embeddingModel) {
        /* 벡터 스토어 빈 생성 -> pgVectorJdbcTemplate과 임베딩 모델을 사용하여 PgVectorStore 생성 */
        // dimensions : 임베딩 벡터의 차원 수 (OpenAI text-embedding-3-small 기준 1536)
        // distanceType : 벡터 간 유사도 계산 방식 (코사인 거리)
        // initializeSchema : true로 설정하면 테이블과 인덱스를 자동으로 생성
        return PgVectorStore.builder(pgVectorJdbcTemplate, embeddingModel)
                .dimensions(1536)  // OpenAI text-embedding-3-small 기준
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .initializeSchema(true)  // 테이블, 인덱스 자동 생성
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.enabled", havingValue = "false")
    public VectorStore noOpVectorStore() {
        return new VectorStore() {
            @Override
            public void add(List<Document> documents) {
            }

            @Override
            public void delete(List<String> ids) {
            }

            @Override
            public void delete(Filter.Expression filterExpression) {
            }

            @Override
            public List<Document> similaritySearch(SearchRequest request) {
                return List.of();
            }
        };
    }
}
