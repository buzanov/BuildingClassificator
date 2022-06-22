package api.model

import java.util.UUID


data class Way(
    val internal_id: Int,
    val tag: Collection<Tag>,
    val refs: Collection<Long>,
    val id: UUID? = UUID.randomUUID()
) {
    override fun toString(): String {
        return "$internal_id $tag $refs $id"
    }
}