package api.repository

import api.model.*
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import javax.swing.tree.RowMapper

@Component
open class CommonSaveRepository(
    private val namedJdbcTemplate: NamedParameterJdbcTemplate
) {

    //Way
    private val SQL_FIND_BY_INTERNAL_ID = "SELECT id FROM tb_way WHERE internal_id = :internal_id";
    private val SQL_SAVE_WAY = "INSERT INTO tb_way (id, internal_id) values (:id::uuid, :internal_id)"
    private val SQL_SAVE_WAY_TO_NODE = "INSERT INTO tb_way_node (way_id, node_id) values (:way_id::uuid, :node_id)"
    private val SQL_SAVE_WAY_TO_TAG = "INSERT INTO tb_way_tag(way_id, tag_id) values (:way_id::uuid, :tag_id::uuid)"
    private val SQL_SAVE_TAG = "INSERT INTO tb_tag (id, key, value) values (:id::uuid, :key, :value)"

    //Node
    private val SQL_SAVE_NODE =
        "INSERT INTO tb_node (id, lat, lon, internal_id) VALUES (:id::uuid, :lat, :lon, :internal_id)"
    private val SQL_SAVE_NODE_TO_TAG =
        "INSERT INTO tb_node_tag (node_id, tag_id) VALUES (:node_id::uuid, :tag_id::uuid)"

    //Relation
    private val SQL_SAVE_RELATION = "INSERT INTO tb_relation (id, internal_id) VALUES (:id::uuid, :internal_id)"
    private val SQL_SAVE_RELATION_TO_MEMBER =
        "INSERT INTO tb_relation_member (relation_id, member_id) VALUES (:relation_id::uuid, :member_id::uuid)"
    private val SQL_SAVE_RELATION_TO_TAG =
        "INSERT INTO tb_relation_tag (relation_id, tag_id) VALUES (:relation_id::uuid, :tag_id::uuid)"

    //Member
    val SQL_SAVE_MEMBER = "INSERT INTO tb_member (type, ref, role, id) VALUES (:type, :ref, :role, :id::uuid)"


    fun save(way: Way) {
        val src = MapSqlParameterSource()
        src.addValue("id", way.id.toString())
        src.addValue("internal_id", way.internal_id)


        namedJdbcTemplate.update(SQL_SAVE_WAY, src)
        saveTags(way.tag)

        val wayTags = mutableListOf<MapSqlParameterSource>()
        way.tag.forEach {
            val wayTag = MapSqlParameterSource()
            wayTag.addValue("tag_id", it.id.toString())
            wayTag.addValue("way_id", way.id.toString())

            wayTags.add(wayTag)
        }
        namedJdbcTemplate.batchUpdate(SQL_SAVE_WAY_TO_TAG, wayTags.toTypedArray())

        val wayNodes = mutableListOf<MapSqlParameterSource>()
        way.refs.forEach {
            val wayTag = MapSqlParameterSource()
            wayTag.addValue("node_id", it)
            wayTag.addValue("way_id", way.id.toString())

            wayNodes.add(wayTag)
        }
        namedJdbcTemplate.batchUpdate(SQL_SAVE_WAY_TO_NODE, wayNodes.toTypedArray())
    }

    fun saveNodesOfWay(way: Way) {
        val src = MapSqlParameterSource()
        src.addValue("internal_id", way.internal_id)
        val id = namedJdbcTemplate.queryForObject(SQL_FIND_BY_INTERNAL_ID, src) { rs, _ -> rs.getString("id") }
        if (id == null) {
            save(way)
            return
        }
        val wayNodes = mutableListOf<MapSqlParameterSource>()
        way.refs.forEach {
            val wayTag = MapSqlParameterSource()
            wayTag.addValue("node_id", it)
            wayTag.addValue("way_id", id)

            wayNodes.add(wayTag)
        }
        namedJdbcTemplate.batchUpdate(SQL_SAVE_WAY_TO_NODE, wayNodes.toTypedArray())
    }

    fun saveMembers(members: Collection<Member>) {
        val src = mutableListOf<MapSqlParameterSource>()
        members.forEach {
            val source = MapSqlParameterSource()

            source.addValue("id", it.id.toString())
            source.addValue("ref", it.ref)
            source.addValue("role", it.role)
            source.addValue("type", it.type.toString())

            src.add(source)
        }
        namedJdbcTemplate.batchUpdate(SQL_SAVE_MEMBER, src.toTypedArray())
    }

    fun saveTags(tag: Collection<Tag>) {
        val src = mutableListOf<MapSqlParameterSource>()
        tag.forEach {
            val source = MapSqlParameterSource()

            source.addValue("id", it.id.toString())
            source.addValue("key", it.k)
            source.addValue("value", it.v)

            src.add(source)

        }
        namedJdbcTemplate.batchUpdate(SQL_SAVE_TAG, src.toTypedArray())
    }

    fun save(node: Node) {
        val src = MapSqlParameterSource()
        src.addValue("internal_id", node.internal_id)
        src.addValue("id", node.id.toString())
        src.addValue("lat", node.lat)
        src.addValue("lon", node.lon)

        namedJdbcTemplate.update(SQL_SAVE_NODE, src)
        saveTags(node.tag)

        val nodeTags = mutableListOf<MapSqlParameterSource>()
        node.tag.forEach {
            val wayTag = MapSqlParameterSource()
            wayTag.addValue("tag_id", it.id.toString())
            wayTag.addValue("node_id", node.id.toString())

            nodeTags.add(wayTag)
        }
        namedJdbcTemplate.batchUpdate(SQL_SAVE_NODE_TO_TAG, nodeTags.toTypedArray())
        println("Saved node ${node.internal_id}")
    }

    fun save(relation: Relation) {
        val src = MapSqlParameterSource()
        src.addValue("internal_id", relation.internal_id)
        src.addValue("id", relation.id.toString())

        namedJdbcTemplate.update(SQL_SAVE_RELATION, src)
        saveTags(relation.tag)
        saveMembers(relation.member)

        val relationsTags = mutableListOf<MapSqlParameterSource>()
        relation.tag.forEach {
            val relationTag = MapSqlParameterSource()
            relationTag.addValue("tag_id", it.id.toString())
            relationTag.addValue("relation_id", relation.id.toString())

            relationsTags.add(relationTag)
        }

        namedJdbcTemplate.batchUpdate(SQL_SAVE_RELATION_TO_TAG, relationsTags.toTypedArray())

        val relationMembers = mutableListOf<MapSqlParameterSource>()
        relation.member.forEach {
            val relationMember = MapSqlParameterSource()
            relationMember.addValue("member_id", it.id.toString())
            relationMember.addValue("relation_id", relation.id.toString())

            relationMembers.add(relationMember)
        }

        namedJdbcTemplate.batchUpdate(SQL_SAVE_RELATION_TO_MEMBER, relationMembers.toTypedArray())
    }

}
