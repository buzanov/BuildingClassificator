package api.model

class Coordinate(
    val lon: Double = 0.0,
    val lat: Double = 0.0
) {
    override fun toString(): String {
        return "lat: $lat lon: $lon"
    }
}