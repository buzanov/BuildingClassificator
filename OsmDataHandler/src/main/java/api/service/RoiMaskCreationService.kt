package api.service

import api.model.Building
import api.model.BuildingType
import api.model.Coordinates
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import nu.pattern.OpenCV
import org.opencv.core.Core
import org.opencv.core.CvType.CV_32FC3
import org.opencv.core.CvType.CV_8U
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.highgui.HighGui.toBufferedImage
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgcodecs.Imgcodecs.IMREAD_ANYCOLOR
import org.opencv.imgcodecs.Imgcodecs.imread
import org.opencv.imgproc.Imgproc.INTER_LINEAR
import org.opencv.imgproc.Imgproc.fillConvexPoly
import java.awt.FlowLayout
import java.awt.Polygon
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type
import java.util.*
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.system.exitProcess


class RoiMaskCreationService {

    fun init() {
        OpenCV.loadLocally()

        val result = File("json.json")
        val map: Map<String, List<Building>> = ObjectMapper().readValue(result)

        map.entries.forEach { entry ->
            val imageName = entry.key
            val buildings = entry.value

            val image = imread(
                "D:\\BuildingClassificator\\AOI_3_Paris_Train\\RGB-PanSharpen\\RGB-PanSharpen_$imageName.tif",
                IMREAD_ANYCOLOR
            )
            val mask = Mat.zeros(image.size(), CV_8U)

            val normalized = Mat(650, 650, CV_32FC3)
            Core.normalize(image, normalized, 0.0, 255.0, Core.NORM_MINMAX)
            buildings.forEachIndexed { _, it ->
                val category = BuildingType.valueOf(it.category)

                if (category != BuildingType.UNDEFINED) {

                    val matOfPoint = MatOfPoint()
                    matOfPoint.fromList(it.points)


                    fillConvexPoly(mask, matOfPoint, Scalar.all(category.ordinal + 1.0), INTER_LINEAR, 0)

                }
            }
            val file = File("D:\\BuildingClassificator\\BuildingClassificator\\mask\\RGB-PanSharpen_${imageName}.png")
            if (!file.exists()) file.createNewFile()
            println(Imgcodecs.imwrite(file.absolutePath, mask))
        }

    }

    fun readBuildingsCsv(trainingCsv: File): Map<String, MutableList<Building>> {
        val buildingsPerImage: MutableMap<String, MutableList<Building>> = HashMap<String, MutableList<Building>>()
        try {
            System.err.println("Reading Buildings CSV")
            val t = System.currentTimeMillis()
            var numLines = 0
            var numBuildings = 0
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
                val tags = File("D:\\BuildingClassificator\\BuildingClassificator\\buildingTypes\\$imageId.txt")
                val scanner = Scanner(tags)
                var idx = 0
                while (scanner.hasNext()) {
                    val json = scanner.nextLine()
                    if (idx == buildingId - 1) {
                        building.category =
                            BuildingType.values().firstOrNull { json.contains(it.name) }?.name ?: "UNDEFINED"
                    }
                    idx++
                }
                scanner.close()
                buildings!!.add(building)
                var s = cols[2]
                var p1 = s.indexOf(s1)
                while (p1 >= 0) {
                    val p2 = s.indexOf(s2, p1 + s1.length)
                    val pts = s.substring(p1 + s1.length, p2).split("),(").toTypedArray()
                    for (i in pts.indices) {
                        val pt = pts[i].split(",").toTypedArray()
                        for (element in pt) {
                            val coord = element.split(" ").toTypedArray()
                            val point = Point(coord[0].toDouble(), coord[1].toDouble())
                            building.points.add(point)
                        }
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
            System.err.println("\t  Elapsed Time: " + (System.currentTimeMillis() - t) + " ms")
            System.err.println()
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(-1)
        }
        return buildingsPerImage
    }
}

fun main() {
    RoiMaskCreationService().init()
}