package com.bookdetails;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class GraphQLProvider {
    @Autowired
    GraphQLDataFetchers graphQLDataFetchers;

    private GraphQL graphQL;

    @PostConstruct
    public void init() throws IOException {
        //1. 加载GraphQL Schema文件为字符串
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);

        //2. 将定义的Schema解析为TypeDefinitionRegistry
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);

        //3. 为Schema中定义的方法和字段绑定获取数据的方法
        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query").dataFetcher("books", graphQLDataFetchers.getAllBooksDataFetcher()))
                .type(newTypeWiring("Query").dataFetcher("authors", graphQLDataFetchers.getAuthorDataFetcher()))
                .type(newTypeWiring("Book").dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcher()))
                .type(newTypeWiring("Author").dataFetcher("address", graphQLDataFetchers.getAuthorAddressDataFetcher()))
                .type(newTypeWiring("Query").dataFetcher("getBookByPageCount", graphQLDataFetchers.getBookByPageCount()))
                .build();

        //4.  将TypeDefinitionRegistry与RuntimeWiring结合起来生成GraphQLSchema
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);

        //5. 实例化GraphQL, GraphQL为执行GraphQL查询语言的入口
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }
    private GraphQLSchema buildSchema(String sdl) {

        return null;
    }
    private RuntimeWiring buildWiring() {
        //5. 实例化GraphQL, GraphQL为执行GraphQL查询语言的入口
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query").dataFetcher("books", graphQLDataFetchers.getAllBooksDataFetcher()))
                .type(newTypeWiring("Query").dataFetcher("authors", graphQLDataFetchers.getAuthorDataFetcher()))
                .type(newTypeWiring("Book").dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcher()))
                .type(newTypeWiring("Author").dataFetcher("address", graphQLDataFetchers.getAuthorAddressDataFetcher()))
                .type(newTypeWiring("Query").dataFetcher("getBookByPageCount", graphQLDataFetchers.getBookByPageCount()))
                .build();
    }

    // 执行GraphQL语言的入口
    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

}
