package api.configuration

import api.consumer.Consumer
import api.model.ParsedEntity
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.JmsListenerConfigurer
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.config.JmsListenerContainerFactory
import org.springframework.jms.config.JmsListenerEndpointRegistrar
import org.springframework.jms.config.SimpleJmsListenerEndpoint
import org.springframework.jms.support.converter.MappingJackson2MessageConverter
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.jms.support.converter.MessageType
import org.springframework.util.backoff.FixedBackOff
import java.util.*
import javax.jms.ConnectionFactory
import javax.jms.MessageListener

@Configuration
@ConditionalOnProperty(
    value = ["ru.itis.buzanov.data.processing"],
    matchIfMissing = false
)
open class ApplicationConfiguration(
    private val consumer: Consumer
) : JmsListenerConfigurer {

    override fun configureJmsListeners(registrar: JmsListenerEndpointRegistrar) {
        val endpoint = SimpleJmsListenerEndpoint()
        endpoint.id = UUID.randomUUID().toString()
        endpoint.destination = "Entities.In"
        endpoint.messageListener = MessageListener { message ->
            @Suppress("UNCHECKED_CAST")
            val convertedMessage = messageConverter().fromMessage(message) as ParsedEntity
            consumer.consume(convertedMessage)
        }
        endpoint.concurrency = "50"
        registrar.registerEndpoint(endpoint, jmsContainerFactory())

    }

    @Bean
    open fun connectionFactory(): ConnectionFactory {
        return ActiveMQConnectionFactory().also {
            it.brokerURL = "tcp://localhost:61616"
            it.password = "postgres"
            it.userName = "postgres"
        }
    }

    @Bean
    open fun jmsContainerFactory(): JmsListenerContainerFactory<*> {
        val factory = DefaultJmsListenerContainerFactory()
        factory.setMessageConverter(messageConverter())
        factory.setBackOff(FixedBackOff(1, 10))
        factory.setConnectionFactory(connectionFactory())
        println("JMS CONTAINER FACTORY CONFIGURATION")
        return factory
    }

    @Bean
    open fun messageConverter(): MessageConverter {
        return MappingJackson2MessageConverter().also {
            it.setObjectMapper(
                ObjectMapper().registerKotlinModule()
            )
            it.setTargetType(MessageType.TEXT)
            it.setTypeIdPropertyName("_type")
            it.setTypeIdMappings(
                mapOf(
                    ParsedEntity::class.java.simpleName to ParsedEntity::class.java
                )
            )
        }
    }

}