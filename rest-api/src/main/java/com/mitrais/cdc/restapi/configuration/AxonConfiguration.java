package com.mitrais.cdc.restapi.configuration;

import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.distributed.jgroups.JGroupsConnector;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.CommandGatewayFactoryBean;
import org.axonframework.commandhandling.interceptors.BeanValidationInterceptor;
import org.axonframework.serializer.xml.XStreamSerializer;
import org.jgroups.JChannel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class AxonConfiguration {

    @Bean
    public XStreamSerializer xstreamSerializer() {
        return new XStreamSerializer();
    }

    @Bean
    @Qualifier("distributedCommandGateway")
    public CommandGatewayFactoryBean<CommandGateway> commandGatewayFactoryBean() {
        CommandGatewayFactoryBean<CommandGateway> factory = new CommandGatewayFactoryBean<>();
        factory.setCommandBus(distributedCommandBus());

        return factory;
    }

    @Bean
    public JGroupsConnector getJGroupConnector() {
        try {

            JGroupsConnector connector = new JGroupsConnector(new JChannel("udp_config.xml"), "myCluster",
                    localSegment(), xstreamSerializer());
            connector.connect(100);
            return connector;
        } catch (Exception ex) {
            System.out.println("Exception in  jgroups clusster"+ ex);
        }
        return null;
    }

    @Bean
    @Qualifier("distributedCommandBus")
    public DistributedCommandBus distributedCommandBus() {
        DistributedCommandBus distributedCommandBus = new DistributedCommandBus(getJGroupConnector());
        return distributedCommandBus;
    }


    @Bean
    @Qualifier("localCommandBus")
    public SimpleCommandBus localSegment() {
        SimpleCommandBus simpleCommandBus = new SimpleCommandBus();
        simpleCommandBus.setDispatchInterceptors(Arrays.asList(new BeanValidationInterceptor()));
        return simpleCommandBus;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}
