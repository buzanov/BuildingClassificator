package api.model

import java.util.*

class Tag(
    val id: UUID? = UUID.randomUUID(),
    val k: String,
    val v: String
) {
    constructor(map: Map<String, String>) : this(k = map["k"]!!, v = map["v"]!!)
}
