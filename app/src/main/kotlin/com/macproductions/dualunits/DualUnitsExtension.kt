package com.macproductions.dualunits

import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.models.DataType

/**
 * Karoo extension providing data fields that show metric (large) and imperial (small)
 * side by side, to help get used to km/h and km.
 */
class DualUnitsExtension : KarooExtension("dualunits", "1.0") {

    private val karooSystem by lazy { KarooSystemService(this) }

    override val types by lazy {
        listOf(
            DualUnitDataType(
                karooSystem, extension,
                typeId = "speed",
                sourceType = DataType.Type.SPEED,
                sourceField = DataType.Field.SPEED,
                measure = Measure.SPEED,
                previewValue = 8.9, // m/s ≈ 32 km/h
            ),
            DualUnitDataType(
                karooSystem, extension,
                typeId = "avg-speed",
                sourceType = DataType.Type.AVERAGE_SPEED,
                sourceField = DataType.Field.AVERAGE_SPEED,
                measure = Measure.SPEED,
                previewValue = 7.6, // m/s ≈ 27 km/h
            ),
            DualUnitDataType(
                karooSystem, extension,
                typeId = "lap-avg-speed",
                sourceType = DataType.Type.AVERAGE_SPEED_LAP,
                sourceField = DataType.Field.AVERAGE_SPEED,
                measure = Measure.SPEED,
                previewValue = 8.1, // m/s ≈ 29 km/h
            ),
            DualUnitDataType(
                karooSystem, extension,
                typeId = "distance",
                sourceType = DataType.Type.DISTANCE,
                sourceField = DataType.Field.DISTANCE,
                measure = Measure.DISTANCE,
                previewValue = 42_195.0, // metres
            ),
            DualUnitDataType(
                karooSystem, extension,
                typeId = "distance-to-go",
                sourceType = DataType.Type.DISTANCE_TO_DESTINATION,
                sourceField = DataType.Field.DISTANCE_TO_DESTINATION,
                measure = Measure.DISTANCE,
                previewValue = 18_500.0, // metres
            ),
        )
    }

    override fun onCreate() {
        super.onCreate()
        karooSystem.connect()
    }

    override fun onDestroy() {
        karooSystem.disconnect()
        super.onDestroy()
    }
}
