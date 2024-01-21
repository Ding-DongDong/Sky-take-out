package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类，用于注册WebSocket的Bean
 */
@Configuration
public class WebSocketConfiguration {
    /**
     * WebSocket 配置类（WebSocketConfiguration）：
     * 这个配置类使用了 @Configuration 注解，表明它是一个配置类，Spring 会在启动时加载并处理它。
     * 通过 @Bean 注解，它创建了一个 ServerEndpointExporter Bean，该 Bean 是 Spring WebSocket 标准的端点导出器。
     * ServerEndpointExporter 的作用是将使用 @ServerEndpoint 注解声明的 WebSocket 端点注册到 Spring 容器中。
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
