package api.service

import api.model.EntityType
import api.model.ParsedEntity
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import javax.jms.ConnectionFactory
import javax.jms.Session


@Component
@ConditionalOnProperty(
    value = ["ru.itis.buzanov.data.processing"],
    matchIfMissing = false
)
class DataReadingService(
    private val connectionFactory: ConnectionFactory,
    private val messageConverter: MessageConverter
) : InitializingBean {
    private val osmFilePath =
        "D:\\BuildingClassificator\\BuildingClassificator\\OsmClient\\src\\main\\resources\\planet.osm"
    private val scanner = Scanner(File(osmFilePath))

    fun produce() {
        val connection = connectionFactory.createConnection()
        connection.start()

        val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        val destination = session.createQueue("Entities.In")
        val producer = session.createProducer(destination)

        var str: String
        val entity = mutableListOf<String>()
        var handled = true
        while (scanner.hasNext()) {
            str = scanner.nextLine().trim()
            if (handled) {
                if (str.startsWith("<relation")) {
                    handled = false
                } else
                    continue
            }
            entity.clear()
            val type: EntityType = if (str.startsWith("<node")) {
                EntityType.NODE
            } else if (str.startsWith("<way")) {
                EntityType.WAY
            } else if (str.startsWith("<relation")) {
                EntityType.RELATION
            } else {
                println("Skipped")
                continue
            }
            val hasEmbeddedEntities = !str.endsWith("/>")
            entity.add(str)
            if (hasEmbeddedEntities) {
                str = scanner.nextLine().trim()
                do {
                    entity.add(str)
                    str = scanner.nextLine().trim()
                } while (!str.startsWith("</"))
                entity.add(str)
            }
            val e = ParsedEntity(entity, hasEmbeddedEntities, type)
            println("Produced entity ${e.type}")

            val message = messageConverter.toMessage(e, session)
            producer.send(message)
        }
    }

    override fun afterPropertiesSet() {
        val thread = Thread { produce() }
        thread.priority = 10
        thread.start()
    }
}

