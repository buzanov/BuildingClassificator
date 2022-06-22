package ru.itis.buzanov.util

import java.awt.geom.Area
import java.awt.geom.Line2D
import java.awt.geom.PathIterator

class Util {
    companion object {
        private const val delimiter = '='
        private const val quote = '"'
        private const val space = ' '

        fun areaVal(shape: Area): Double {
            val i = shape.getPathIterator(null)
            var a = 0.0
            val coords = DoubleArray(6)
            var startX = Double.NaN
            var startY = Double.NaN
            val segment: Line2D = Line2D.Double(Double.NaN, Double.NaN, Double.NaN, Double.NaN)
            while (!i.isDone) {
                val segType = i.currentSegment(coords)
                val x = coords[0]
                val y = coords[1]
                when (segType) {
                    PathIterator.SEG_CLOSE -> {
                        segment.setLine(segment.x2, segment.y2, startX, startY)
                        a += area(segment)
                        startX = Double.NaN
                        startY = Double.NaN
                        segment.setLine(Double.NaN, Double.NaN, Double.NaN, Double.NaN)
                    }
                    PathIterator.SEG_LINETO -> {
                        segment.setLine(segment.x2, segment.y2, x, y)
                        a += area(segment)
                    }
                    PathIterator.SEG_MOVETO -> {
                        startX = x
                        startY = y
                        segment.setLine(Double.NaN, Double.NaN, x, y)
                    }
                }
                i.next()
            }
            return if (java.lang.Double.isNaN(a)) {
                throw IllegalArgumentException("PathIterator contains an open path")
            } else {
                0.5 * Math.abs(a)
            }
        }

        private fun area(seg: Line2D): Double {
            return seg.x1 * seg.y2 - seg.x2 * seg.y1
        }


        fun parseCols(line: String): List<String> {
            val sb = StringBuilder()
            val cols: MutableList<String> = ArrayList()
            var inside = false
            for (c in line) {
                if (c == '\"') {
                    inside = !inside
                } else if (c == ',' && !inside) {
                    cols.add(sb.toString())
                    sb.delete(0, sb.length)
                } else {
                    sb.append(c)
                }
            }
            cols.add(sb.toString())
            return cols
        }

        fun splitToProperties(string: String): Map<String, String> {
            val result = mutableMapOf<String, String>()
            var keyProcessing = true
            var valueProcessing = false
            val key = mutableListOf<Char>()
            val value = mutableListOf<Char>()
            for (c in string) {
                if (c == space) {
                    if (valueProcessing) {
                        value.add(c)
                    } else {
                        result[key.joinToString(separator = "")] = value.joinToString(separator = "")
                        key.clear()
                        value.clear()
                        keyProcessing = true
                    }
                } else if (c == delimiter) {
                    keyProcessing = false
                    valueProcessing = false
                } else if (c == quote) {
                    if (valueProcessing) {
                        valueProcessing = false
                        keyProcessing = true
                    } else {
                        valueProcessing = true
                    }
                } else if (keyProcessing) {
                    key.add(c)
                } else {
                    value.add(c)
                }
            }
            result[key.joinToString(separator = "")] = value.joinToString(separator = "")
            key.clear()
            value.clear()

            return result
        }
    }
}

fun <T> Boolean.then(then: T, or: T): T {
    return if (this) {
        then
    } else or
}

fun String.cut(from: Int, to: Int): String {
    val length = this.length
    return if (to < 0) {
        this.substring(from, length + to)
    } else {
        substring(from, to)
    }
}