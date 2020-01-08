package com.mitrais.cdc.eventhandler.configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.eventhandling.ClusteringEventBus;
import org.axonframework.eventhandling.DefaultClusterSelector;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventBusTerminal;
import org.axonframework.eventhandling.SimpleCluster;
import org.axonframework.eventhandling.amqp.DefaultAMQPMessageConverter;
import org.axonframework.eventhandling.amqp.spring.ListenerContainerLifecycleManager;
import org.axonframework.eventhandling.amqp.spring.SpringAMQPTerminal;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerBeanPostProcessor;
import org.axonframework.serializer.xml.XStreamSerializer;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfiguration {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${ecom.amqp.rabbit.address}")
    private String rabbitMQAddress;

    @Value("${ecom.amqp.rabbit.username}")
    private String rabbitMQUser;

    @Value("${ecom.amqp.rabbit.password}")
    private String rabbitMQPassword;

    @Value("${ecom.amqp.rabbit.vhost}")
    private String rabbitMQVhost;

    @Value("${ecom.amqp.rabbit.exchange}")
    private String rabbitMQExchange;

    @Value("${ecom.amqp.rabbit.queue}")
    private String rabbitMQQueue;

    @Bean
    public XStreamSerializer xstreamSerializer() {
        return new XStreamSerializer();
    }


    // Connection Factory
    @Bean
    public ConnectionFactory connectionFactory() {

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(rabbitMQAddress);
        connectionFactory.setUsername(rabbitMQUser);
        connectionFactory.setPassword(rabbitMQPassword);
        connectionFactory.setVirtualHost(rabbitMQVhost);
        connectionFactory.setConnectionTimeout(500000);
        connectionFactory.setRequestedHeartBeat(20);
        return connectionFactory;
    }

    @Bean
    public FanoutExchange eventBusExchange() {

        return new FanoutExchange(rabbitMQExchange, true, false);
    }

    // Event bus queue
    @Bean
    public Queue eventBusQueue() {

        return new Queue(rabbitMQQueue, true, false, false);
    }

    // binding queue to exachange
    @Bean
    public Binding binding() {

        return BindingBuilder.bind(eventBusQueue()).to(eventBusExchange());
    }

    // Event bus
    @Bean
    public EventBus eventBus() {

        ClusteringEventBus clusteringEventBus = new ClusteringEventBus(new DefaultClusterSelector(simpleCluster()),
                terminal());
        return clusteringEventBus;
    }

    // Message converter
    @Bean
    public DefaultAMQPMessageConverter defaultAMQPMessageConverter() {

        return new DefaultAMQPMessageConverter(xstreamSerializer());
    }

    // Message listener configuration
    @Bean
    ListenerContainerLifecycleManager listenerContainerLifecycleManager() {

        ListenerContainerLifecycleManager listenerContainerLifecycleManager = new ListenerContainerLifecycleManager();
        listenerContainerLifecycleManager.setConnectionFactory(connectionFactory());
        return listenerContainerLifecycleManager;
    }

    // Event listener
    @Bean
    public AnnotationEventListenerBeanPostProcessor annotationEventListenerBeanPostProcessor() {

        AnnotationEventListenerBeanPostProcessor processor = new AnnotationEventListenerBeanPostProcessor();
        processor.setEventBus(eventBus());
        return processor;
    }


    // Terminal
    @Bean
    public EventBusTerminal terminal() {

        SpringAMQPTerminal terminal = new SpringAMQPTerminal();
        terminal.setConnectionFactory(connectionFactory());
        terminal.setSerializer(xstreamSerializer());
        terminal.setExchangeName(rabbitMQExchange);
        terminal.setListenerContainerLifecycleManager(listenerContainerLifecycleManager());
        terminal.setDurable(true);
        terminal.setTransactional(true);
        return terminal;
    }

    // Cluster definition
    // @Bean
    SimpleCluster simpleCluster() {

        SimpleCluster simpleCluster = new SimpleCluster(rabbitMQQueue);
        return simpleCluster;
    }
}
