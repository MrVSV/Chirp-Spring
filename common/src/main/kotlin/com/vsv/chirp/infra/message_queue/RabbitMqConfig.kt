@file:Suppress("DEPRECATION", "removal")

package com.vsv.chirp.infra.message_queue

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.vsv.chirp.domain.events.ChirpEvent
import com.vsv.chirp.domain.events.chat.ChatEventConstants
import com.vsv.chirp.domain.events.user.UserEventConstants
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class RabbitMqConfig {

    @Bean
    fun messageConverter(): Jackson2JsonMessageConverter {
        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            findAndRegisterModules()

            val polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(ChirpEvent::class.java)
                .allowIfSubType("java.util") // Allow java lists
                .allowIfSubType("kotlin.collections") // Allow kotlin collections
            .build()

            activateDefaultTyping(
                polymorphicTypeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL
            )
        }
        return Jackson2JsonMessageConverter(objectMapper).apply {
            typePrecedence = Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID
        }
    }

    @Bean
    fun rabbitListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        transactionManager: PlatformTransactionManager,
        messageConverter: MessageConverter
    ): SimpleRabbitListenerContainerFactory {
        return SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setTransactionManager(transactionManager)
            setMessageConverter(messageConverter)
            setChannelTransacted(true)
        }
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: Jackson2JsonMessageConverter,
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    @Bean
    fun userExchange() = TopicExchange(
        UserEventConstants.USER_EXCHANGE,
        true,
        false
    )

    @Bean
    fun chatExchange() = TopicExchange(
        ChatEventConstants.CHAT_EXCHANGE,
        true,
        false
    )

    @Bean
    @Qualifier(MessageQueues.NOTIFICATION_USER_EVENTS)
    fun notificationUserEventQueue() = Queue(
        MessageQueues.NOTIFICATION_USER_EVENTS,
        true
    )

    @Bean
    fun notificationUserEventsBinding(
        @Qualifier(MessageQueues.NOTIFICATION_USER_EVENTS)
        notificationUserEventsQueue: Queue,
        userExchange: TopicExchange,
    ): Binding {
        return BindingBuilder
            .bind(notificationUserEventsQueue)
            .to(userExchange)
            .with("user.*")
    }

    @Bean
    @Qualifier(MessageQueues.CHAT_USER_EVENTS)
    fun chatUserEventQueue() = Queue(
        MessageQueues.CHAT_USER_EVENTS,
        true
    )

    @Bean
    fun chatUserEventsBinding(
        @Qualifier(MessageQueues.CHAT_USER_EVENTS)
        chatUserEventsQueue: Queue,
        userExchange: TopicExchange,
    ): Binding {
        return BindingBuilder
            .bind(chatUserEventsQueue)
            .to(userExchange)
            .with("user.*")
    }

    @Bean
    @Qualifier(MessageQueues.NOTIFICATION_CHAT_EVENTS)
    fun notificationChatEventQueue() = Queue(
        MessageQueues.NOTIFICATION_CHAT_EVENTS,
        true
    )

    @Bean
    fun notificationChatEventsBinding(
        @Qualifier(MessageQueues.NOTIFICATION_CHAT_EVENTS)
        notificationChatEventsQueue: Queue,
        chatExchange: TopicExchange,
    ): Binding {
        return BindingBuilder
            .bind(notificationChatEventsQueue)
            .to(chatExchange)
            .with(ChatEventConstants.CHAT_NEW_MESSAGE)
    }
}