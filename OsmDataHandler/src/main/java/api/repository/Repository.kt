package api.repository

import api.model.Tag
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class Repository(
    val jdbcTemplate: NamedParameterJdbcTemplate
) {

    private val FIND_NODES_SQL =
        "SELECT * FROM tb_node where lat between :minLat and :maxLat and lon between :minLon and :maxLon"

    private val FIND_WAYS_SQL =
        "SELECT way_id FROM tb_way_node where node_id in (:nodeIds)"

    private val FIND_TAGS_BY_WAY_SQL =
        "SELECT tag_id FROM tb_way_tag where way_id in (:wayIds)"

    private val FIND_TAGS_BY_NODES_SQL =
        "SELECT tag_id FROM tb_node_tag where node_id in (:nodeIds)"

    private val FIND_TAGS_SQL =
        "SELECT * FROM tb_tag where id in (:tagIds)"


    private val SQL_FIND_TAGS_BY_NODES = """select tt.*
from tb_node
         left join tb_way_node on tb_node.internal_id = tb_way_node.node_id
         left join tb_way_tag on tb_way_node.way_id = tb_way_tag.way_id
         left join tb_way on tb_way_node.way_id = tb_way.id
         left join tb_member on (tb_member.type = 'node' and tb_member.ref = tb_node.internal_id) or
                                (tb_member.type = 'way' and tb_member.ref = tb_way.internal_id)
         left join tb_node_tag on tb_node.id = tb_node_tag.node_id
         left join tb_relation_member trm on tb_member.id = trm.member_id
         left join tb_relation_tag on tb_relation_tag.relation_id = trm.relation_id
         left join tb_tag tt on tb_node_tag.tag_id = tt.id or tb_way_tag.tag_id = tt.id or tb_relation_tag.tag_id = tt.id
where lat between :minLat::numeric and :maxLat::numeric
  and lon between :minLon::numeric and :maxLon::numeric;"""

    /**
     * first - id (uuid)
     * second - internalId (long)
     */
    fun getNodes(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): List<Pair<String, String>> {
        val start = Date()
        val map = MapSqlParameterSource()
        map.addValue("minLat", minLat)
        map.addValue("minLon", minLon)
        map.addValue("maxLat", maxLat)
        map.addValue("maxLon", maxLon)

        val list = jdbcTemplate.query(FIND_NODES_SQL, map) { rs, _ ->
            val nodeId = rs.getString("id")
            val nodeInternalId = rs.getString("internal_id")
            return@query Pair(nodeId, nodeInternalId)
        }

        println("Getted nodes. Elapsed time: ${Date().time - start.time} ms")
        return list
    }

    fun findTags(minLat: String, maxLat: String, minLon: String, maxLon: String): List<Tag> {
        val start = Date()
        val map = MapSqlParameterSource()
        map.addValue("minLat", minLat)
        map.addValue("minLon", minLon)
        map.addValue("maxLat", maxLat)
        map.addValue("maxLon", maxLon)


        val list = jdbcTemplate.query(SQL_FIND_TAGS_BY_NODES, map) { rs, _ ->
            val id = UUID.fromString(rs.getString("id"))
            val key = rs.getString("key")
            val value = rs.getString("value")

            return@query Tag(id = id, k = key, v = value)
        }

        println("Finding tags by nodes. Elapsed time: ${Date().time - start.time} ms. Size: ${list.size}")
        return list
    }

    fun getWayOfNodes(nodeInternalIds: List<Long>): List<UUID> {
        if (nodeInternalIds.isEmpty()) {
            return mutableListOf()
        }
        val start = Date()
        val map = MapSqlParameterSource()
        map.addValue("nodeIds", nodeInternalIds)

        val list = jdbcTemplate.query(FIND_WAYS_SQL, map) { rs, _ ->
            return@query UUID.fromString(rs.getString(0))
        }

        println("Getted ways by nodes. Elapsed time: ${Date().time - start.time} ms")
        return list
    }

    fun getTagIdsByWayIds(wayIds: List<UUID>): List<UUID> {
        if (wayIds.isEmpty()) {
            return mutableListOf()
        }
        val start = Date()
        val map = MapSqlParameterSource()
        map.addValue("wayIds", wayIds)

        val list = jdbcTemplate.query(FIND_TAGS_BY_WAY_SQL, map) { rs, _ ->
            return@query UUID.fromString(rs.getString(0))
        }
        println("Getted tags by ways. Elapsed time: ${Date().time - start.time} ms")
        return list
    }

    fun getTagIdsByNodeIds(nodeIds: List<UUID>): MutableList<UUID> {
        if (nodeIds.isEmpty()) {
            return mutableListOf()
        }
        val start = Date()
        val map = MapSqlParameterSource()
        map.addValue("nodeIds", nodeIds)

        val list = jdbcTemplate.query(FIND_TAGS_BY_NODES_SQL, map) { rs, _ ->
            return@query UUID.fromString(rs.getString(0))
        }
        println("Getted tags by nodes. Elapsed time: ${Date().time - start.time} ms")
        return list.toMutableList()
    }

    fun getTags(tagIds: List<UUID>): List<Tag> {
        if (tagIds.isEmpty()) {
            return mutableListOf()
        }
        val start = Date()
        val map = MapSqlParameterSource()
        map.addValue("tagIds", tagIds)

        val list = jdbcTemplate.query(FIND_TAGS_SQL, map) { rs, _ ->
            val id = UUID.fromString(rs.getString("id"))
            val key = rs.getString("key")
            val value = rs.getString("value")

            return@query Tag(id = id, k = key, v = value)
        }

        println("Getted tags by nodes. Elapsed time: ${Date().time - start.time} ms")
        return list
    }
}