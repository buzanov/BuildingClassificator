package api.model

import api.service.Util
import com.fasterxml.jackson.annotation.JsonIgnore
import org.opencv.core.Point
import java.awt.Polygon
import java.awt.geom.Area


class Building(
    var id: Int = 0
) {
    @JsonIgnore
    val inPolygon: MutableList<Polygon> = ArrayList()
    val points: MutableList<Point> = ArrayList()
    var category: String = "UNDEFINED"

    @JsonIgnore
    val outPolygon: MutableList<Polygon> = ArrayList()

    @JsonIgnore
    var realCoordinates: Coordinates = Coordinates()

    @JsonIgnore
    var handled = false

    @JsonIgnore
    private var area: Area? = null

    var areaVal = -1.0


    override fun toString(): String {
        return "IN: $inPolygon OUT: $outPolygon AREA: $area REAL: $realCoordinates"
    }
}