package api.service

import api.model.Building
import api.model.Coordinates
import org.springframework.stereotype.Component
import java.awt.Polygon
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.math.roundToInt
import kotlin.system.exitProcess

@Component
class ResolveCoordinateService {

    fun readBuildingsCsv(trainingCsv: File): Map<String, MutableList<Building>> {
        val buildingsPerImage: MutableMap<String, MutableList<Building>> = HashMap<String, MutableList<Building>>()
        try {
            System.err.println("Reading Buildings CSV")
            val t = System.currentTimeMillis()
            var numLines = 0
            var numBuildings = 0
            var numPolys = 0
            val reader = BufferedReader(FileReader(trainingCsv))
            var line = reader.readLine()
            val s1 = "POLYGON (("
            val s2 = "))"
            while (reader.readLine().also { line = it } != null) {
                numLines++
                val cols: List<String> = Util.parseCols(line)
                var imageId = cols[0]
                if (imageId.lowercase().startsWith("pan_")) imageId = imageId.substring(4)
                var buildings: MutableList<Building>? = buildingsPerImage[imageId]
                if (buildings == null) buildingsPerImage[imageId] = ArrayList<Building>().also {
                    buildings = it
                }
                val buildingId = cols[1].toInt()
                if (buildingId == -1) continue
                val building = Building(buildingId)
                buildings!!.add(building)
                var s = cols[2]
                var p1 = s.indexOf(s1)
                var first = true
                while (p1 >= 0) {
                    val p2 = s.indexOf(s2, p1 + s1.length)
                    val pts = s.substring(p1 + s1.length, p2).split("),(").toTypedArray()
                    for (i in pts.indices) {
                        val pt = pts[i].split(",").toTypedArray()
                        val x = IntArray(pt.size)
                        val y = IntArray(pt.size)
                        for (j in x.indices) {
                            val coord = pt[j].split(" ").toTypedArray()
                            x[j] = coord[0].toDouble().roundToInt()
                            y[j] = coord[1].toDouble().roundToInt()
                        }
                        val poly = Polygon(x, y, x.size)
                        numPolys++
                        if (first) building.inPolygon.add(poly) else building.outPolygon.add(poly)
                    }
                    first = false
                    p1 = p2 + s2.length
                    p1 = s.indexOf(s1, p1)
                }
                s = cols[3]
                p1 = s.indexOf(s1)
                while (p1 >= 0) {
                    val p2 = s.indexOf(s2, p1 + s1.length)
                    val pts = s.substring(p1 + s1.length, p2).split("),(").toTypedArray()
                    for (i in pts.indices) {
                        val pt = pts[i].split(",").toTypedArray()
                        val x = DoubleArray(pt.size)
                        val y = DoubleArray(pt.size)
                        for (j in x.indices) {
                            val coord = pt[j].split(" ").toTypedArray()
                            x[j] = coord[0].toDouble()
                            y[j] = coord[1].toDouble()
                        }
                        val poly = Coordinates(x, y, x.size)
                        building.realCoordinates = poly
                    }
                    p1 = p2 + s2.length
                    p1 = s.indexOf(s1, p1)
                }
                numBuildings++
                println("$imageId is loaded")
            }

            reader.close()
            System.err.println("\t    Lines Read: $numLines")
            System.err.println("\t        Images: " + buildingsPerImage.size)
            System.err.println("\t     Buildings: $numBuildings")
            System.err.println("\t      Polygons: $numPolys")
            System.err.println("\t  Elapsed Time: " + (System.currentTimeMillis() - t) + " ms")
            System.err.println()
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(-1)
        }
        return buildingsPerImage
    }
}