package api.model

import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

data class Relation(
    val internal_id: Int,
    val tag: Collection<Tag>,
    val member: Collection<Member>,
    val id: UUID? = UUID.randomUUID()
) {
}