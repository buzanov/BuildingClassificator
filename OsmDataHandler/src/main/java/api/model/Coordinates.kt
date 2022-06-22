package api.model

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class Coordinates() {
    val coords: MutableList<Coordinate> = mutableListOf()

    constructor(x: DoubleArray, y: DoubleArray, size: Int) : this() {
        for (i in 1..size) {
            coords.add(Coordinate(x[i - 1], y[i - 1]))
        }
    }

    override fun toString(): String {
        return coords.toString()
    }

//    fun getMaxCoordinates(): Pair<String, String> {
//        val format = DecimalFormat("#.####", DecimalFormatSymbols().also { it.decimalSeparator = '.' })
//        format.roundingMode = RoundingMode.UP
//        val maxLon = coords
//            .stream()
//            .max(Comparator.comparingDouble(Coordinate::lon))
//            .orElse(null)
//        val maxLat = coords
//            .stream()
//            .min(Comparator.comparingDouble(Coordinate::lat))
//            .orElse(null)
//        return Pair<String, String>(format.format(maxLon.lon), format.format(maxLat.lat))
////        return Pair<Double, Double>(maxLon.lon, maxLat.lat)
//    }
//
//    fun getMinCoordinates(): Pair<String, String> {
//        val format = DecimalFormat("#.####", DecimalFormatSymbols().also { it.decimalSeparator = '.' })
//        format.roundingMode = RoundingMode.DOWN
//        val minLon = coords
//            .stream()
//            .min(Comparator.comparingDouble(Coordinate::lon))
//            .orElse(null)
//        val minLat = coords
//            .stream()
//            .min(Comparator.comparingDouble(Coordinate::lat))
//            .orElse(null)
////        return Pair<Double, Double>(minLon.lon, minLat.lat)
//        return Pair<String, String>(format.format(minLon.lon), format.format(minLat.lat))
//    }
}