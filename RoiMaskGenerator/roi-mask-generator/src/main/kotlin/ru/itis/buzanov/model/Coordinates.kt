package ru.itis.buzanov.model

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
}