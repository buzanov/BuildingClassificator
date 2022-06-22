package api.model

data class ParsedEntity(
    val entity: List<String>,
    val hasEmbeddedEntities: Boolean = false,
    val type: EntityType
) : java.io.Serializable {
}
