package com.macproductions.dualunits

import java.util.Locale

enum class Measure(val metricUnit: String, val imperialUnit: String) {
    SPEED("km/h", "mph"),
    DISTANCE("km", "mi"),
}

/** Formatted pair ready for display. Null value means "no data". */
data class DualReading(val metric: String, val imperial: String)

private const val MS_TO_KMH = 3.6
private const val MS_TO_MPH = 2.2369362920544
private const val M_PER_KM = 1000.0
private const val M_PER_MILE = 1609.344

/**
 * Karoo reports speed in metres/second and distance in metres.
 */
fun format(measure: Measure, raw: Double?): DualReading {
    if (raw == null || raw.isNaN() || raw < 0) return DualReading("--", "--")
    return when (measure) {
        Measure.SPEED -> DualReading(oneDp(raw * MS_TO_KMH), oneDp(raw * MS_TO_MPH))
        Measure.DISTANCE -> DualReading(distance(raw / M_PER_KM), distance(raw / M_PER_MILE))
    }
}

private fun oneDp(v: Double) = String.format(Locale.US, "%.1f", v)

// Drop the decimal once past 1000 so long rides still fit.
private fun distance(v: Double) =
    if (v < 1000) oneDp(v) else String.format(Locale.US, "%.0f", v)
