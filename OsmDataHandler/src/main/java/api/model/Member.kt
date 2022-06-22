package api.model

import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

class Member(
    val id: UUID,
    val type: Type,
    val ref: Long,
    val role: String?
) {
    constructor(map: Map<String, String>) : this(UUID.randomUUID(), Type.valueOf(map["type"]!!), map["ref"]!!.toLong(), map["role"])

    enum class Type {
        way, node, relation
    }
}
