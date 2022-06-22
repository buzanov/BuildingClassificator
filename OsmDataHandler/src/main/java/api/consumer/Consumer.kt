package api.consumer

import api.model.*
import api.repository.CommonSaveRepository
import api.service.*
import api.service.Util.Companion.splitToProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    value = ["ru.itis.buzanov.data.processing"],
    matchIfMissing = false
)
class Consumer(private val repository: CommonSaveRepository) {

    fun consume(e: ParsedEntity) {
        try {
            when (e.type) {
                EntityType.NODE -> repository.save(processNode(e.entity, e.hasEmbeddedEntities))
                EntityType.RELATION -> repository.save(processRelation(e.entity, e.hasEmbeddedEntities))
                EntityType.WAY -> repository.save(processWay(e.entity, e.hasEmbeddedEntities))
            }
        } catch (e: DuplicateKeyException) {
            println("duplicate")
        }
    }


    private fun processNode(str: List<String>, hasEmbeddedEntities: Boolean): Node {
        val iterable = str.iterator()
        val entityProperty = splitToProperties(iterable.next().cut(6, hasEmbeddedEntities.then(-1, -2)))
        val tags: MutableList<Tag> = mutableListOf()
        if (hasEmbeddedEntities) {
            var embedded = iterable.next()
            do {
                tags.add(Tag(splitToProperties(embedded.cut(5, -2))))
                embedded = iterable.next()
            } while (!embedded.startsWith("</"))
        }
        val node = Node(tags, entityProperty)
        println("Saved node ${node.internal_id}")
        return node
    }

    private fun processRelation(str: List<String>, hasEmbeddedEntities: Boolean): Relation {
        val iterable = str.iterator()
        val entityProperty = splitToProperties(iterable.next().cut(5, hasEmbeddedEntities.then(-1, -2)))
        val tags: MutableList<Tag> = mutableListOf()
        val members: MutableList<Member> = mutableListOf()
        if (hasEmbeddedEntities) {
            var embedded = iterable.next()
            do {
                if (embedded.startsWith("<tag")) {
                    tags.add(Tag(splitToProperties(embedded.cut(5, -2))))
                } else {
                    members.add(Member(splitToProperties(embedded.cut(4, -2))))
                }
                embedded = iterable.next()
            } while (!embedded.startsWith("</"))
        }
        val relation = Relation(entityProperty["id"]!!.toInt(), tags, members)
        println("Saved relation ${relation.internal_id}")
        return relation
    }


    private fun processWay(str: List<String>, hasEmbeddedEntities: Boolean): Way {
        val iterable = str.iterator()
        val entityProperty = splitToProperties(iterable.next().cut(5, hasEmbeddedEntities.then(-1, -2)))
        val tags: MutableList<Tag> = mutableListOf()
        val nodesRef: MutableList<Long> = mutableListOf()
        if (hasEmbeddedEntities) {
            var embedded = iterable.next()
            do {
                if (embedded.startsWith("<tag")) {
                    tags.add(Tag(splitToProperties(embedded.cut(5, -2))))
                } else if (embedded.startsWith("<nd")) {
                    nodesRef.add(splitToProperties(embedded.cut(4, -2))["ref"]!!.toLong())
                } else {
                    println("ERROR")
                }
                embedded = iterable.next()
            } while (!embedded.startsWith("</"))
        }
        val way = Way(entityProperty["id"]!!.toInt(), tags, nodesRef)
        println("Saved way ${way.internal_id}")
        return way
    }
}