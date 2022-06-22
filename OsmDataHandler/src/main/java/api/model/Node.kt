package api.model

import api.service.cut
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class Node(
    val internal_id: Long,
    val tag: MutableList<Tag> = mutableListOf(),
    val lat: Double,
    val lon: Double,
    val id: UUID? = UUID.randomUUID()
) {
    constructor(tag: MutableList<Tag>, map: Map<String, String>) :
            this(
                map["id"]!!.toLong(),
                tag,
                map["lat"]!!.toDouble(),
                map["lon"]!!.toDouble()
            )
}
