package com.mitrais.cdc.handlecommandandcreateevent.configuration;

import com.mitrais.cdc.handlecommandandcreateevent.model.User;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.annotation.AnnotationCommandHandlerBeanPostProcessor;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.distributed.jgroups.JGroupsConnector;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.CommandGatewayFactoryBean;
import org.axonframework.commandhandling.interceptors.BeanValidationInterceptor;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.eventhandling.ClusteringEventBus;
import org.axonframework.eventhandling.DefaultClusterSelector;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventBusTerminal;
import org.axonframework.eventhandling.SimpleCluster;
import org.axonframework.eventhandling.amqp.DefaultAMQPMessageConverter;
import org.axonframework.eventhandling.amqp.spring.ListenerContainerLifecycleManager;
import org.axonframework.eventhandling.amqp.spring.SpringAMQPTerminal;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerBeanPostProcessor;
import org.axonframework.repository.GenericJpaRepository;
import org.axonframework.serializer.xml.XStreamSerializer;
import org.axonframework.unitofwork.SpringTransactionManager;

import org.jgroups.JChannel;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AxonConfiguration {

    @PersistenceContext
    private EntityManager entityManager;

    @Qualifier("transactionManager")
    @Autowired
    protected PlatformTransactionManager txManager;

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

    @Bean
    @Qualifier("distributedCommandGateway")
    public CommandGatewayFactoryBean<CommandGateway> commandGatewayFactoryBean() {

        CommandGatewayFactoryBean<CommandGateway> factory = new CommandGatewayFactoryBean<>();
        factory.setCommandBus(distributedCommandBus());
        return factory;
    }

    @Bean
    @Qualifier("localCommandGateway")
    public CommandGatewayFactoryBean<CommandGateway> localCommandGatewayFactoryBean() {

        CommandGatewayFactoryBean<CommandGateway> factory = new CommandGatewayFactoryBean<>();
        factory.setCommandBus(localSegment());

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
            System.out.println("Exception in  jgroups clusster" + ex);
        }
        return null;
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
        SpringTransactionManager transcationMgr = new SpringTransactionManager(txManager);
        simpleCommandBus.setTransactionManager(transcationMgr);
        simpleCommandBus.setDispatchInterceptors(Arrays.asList(new BeanValidationInterceptor()));
        return simpleCommandBus;
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

    // Command Handler
    @Bean
    public AnnotationCommandHandlerBeanPostProcessor annotationCommandHandlerBeanPostProcessor() {

        AnnotationCommandHandlerBeanPostProcessor processor = new AnnotationCommandHandlerBeanPostProcessor();
        processor.setCommandBus(distributedCommandBus());
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

    @Bean
    @Qualifier("userRepository")
    public GenericJpaRepository<User> userJpaRepository(){
        SimpleEntityManagerProvider entityManagerProvider = new SimpleEntityManagerProvider(entityManager);
        GenericJpaRepository<User> genericJpaRepository = new GenericJpaRepository<>(entityManagerProvider, User.class);
        genericJpaRepository.setEventBus(eventBus());

        return genericJpaRepository;
    }
}
