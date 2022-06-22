package api.service

import api.model.Building
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileWriter
import java.util.Comparator
import java.util.Objects
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import javax.annotation.PostConstruct


@Service
class DataPreparationService(
    private val resolveCoordinateService: ResolveCoordinateService,
    private val repository: api.repository.Repository
) {
    private val filePath =
        "D:\\BuildingClassificator\\AOI_3_Paris_Train\\summaryData\\AOI_3_Paris_Train_Building_Solutions.csv";
    private val buildingTypeFilePath = "buildingTypes/"
    private val mapper = ObjectMapper()

//    @PostConstruct
    fun run() {
        val file = File(filePath)

        val map = resolveCoordinateService.readBuildingsCsv(file)


        map.forEach {
            val image = it.key
            println("$image is start to processing")

            val resultFile = File("${buildingTypeFilePath}$image.txt")
            if (!resultFile.exists()) resultFile.createNewFile()

            val fileWriter = FileWriter(resultFile)
            it.value.forEach { building ->
//                val min = building.realCoordinates.getMinCoordinates()
//                val max = building.realCoordinates.getMaxCoordinates()

//                val tags = repository.findTags(min.second, max.second, min.first, max.first)
//
//                fileWriter.append(mapper.writeValueAsString(tags) + "\n")
//
//                fileWriter.flush()
            }
            fileWriter.close()
        }

    }

    private fun getMinMaxCoords(map: Map<String, MutableList<Building>>) {
        val maxLat = map
            .values
            .stream()
            .flatMap { it.stream() }
            .map { it.realCoordinates.coords }
            .flatMap { it.stream() }
            .max(Comparator.comparingDouble { it.lat })
            .get()
        println(maxLat)
        val minLat = map
            .values
            .stream()
            .flatMap { it.stream() }
            .map { it.realCoordinates.coords }
            .flatMap { it.stream() }
            .min(Comparator.comparingDouble { it.lat })
            .get()
        println(minLat)
        val maxLon = map
            .values
            .stream()
            .flatMap { it.stream() }
            .map { it.realCoordinates.coords }
            .flatMap { it.stream() }
            .max(Comparator.comparingDouble { it.lon })
            .get()
        println(maxLon)
        val minLon = map
            .values
            .stream()
            .flatMap { it.stream() }
            .map { it.realCoordinates.coords }
            .flatMap { it.stream() }
            .min(Comparator.comparingDouble { it.lon })
            .get()
        println(minLon)
    }
}