package com.macproductions.dualunits

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider
import io.hammerhead.karooext.models.ViewConfig
import kotlin.math.max
import kotlin.math.min

// Height of the Karoo's title row above the field (px), and side padding (px).
private const val HEADER_PX = 48
private const val PAD_PX = 8f

private const val UNIT_RATIO = 0.32f // unit label font vs number font

/**
 * Number and unit drawn as two alpha masks the same size as the field.
 * They're tinted by the Karoo when shown, so theme (day/night) colours still apply.
 *
 * We draw the text ourselves rather than using a text view because Android pads
 * text vertically, and at the Karoo's standard size that padding pushes the
 * digits out of the field.
 */
class FieldBitmaps(val number: Bitmap, val unit: Bitmap)

fun renderField(context: Context, config: ViewConfig, value: String, unitLabel: String): FieldBitmaps {
    val w = max(1, config.viewSize.first)
    val h = max(1, config.viewSize.second - HEADER_PX)
    val density = context.resources.displayMetrics.density

    val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT // regular weight, matching the Karoo's built-in fields
        textSize = config.textSize * density // Karoo's standard size for this slot
    }
    val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT }
    val unitText = " $unitLabel"

    // Digit height at this font size, used to centre the number vertically.
    val digit = Rect().also { numPaint.getTextBounds("0", 0, 1, it) }
    var digitH = digit.height().toFloat()

    // Shrink only if needed: too wide for "value unit", or digits taller than the field.
    unitPaint.textSize = numPaint.textSize * UNIT_RATIO
    val totalW = numPaint.measureText(value) + unitPaint.measureText(unitText)
    val scale = min(1f, min((w - 2 * PAD_PX) / totalW, (h - 2 * PAD_PX) / digitH))
    if (scale < 1f) {
        numPaint.textSize *= scale
        unitPaint.textSize = numPaint.textSize * UNIT_RATIO
        digitH *= scale
    }

    val numW = numPaint.measureText(value)
    val width = numW + unitPaint.measureText(unitText)
    val x = when (config.alignment) {
        ViewConfig.Alignment.LEFT -> PAD_PX
        ViewConfig.Alignment.CENTER -> (w - width) / 2
        ViewConfig.Alignment.RIGHT -> w - PAD_PX - width
    }
    val baseline = (h + digitH) / 2

    val number = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
    Canvas(number).drawText(value, x, baseline, numPaint)
    val unit = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
    Canvas(unit).drawText(unitText, x + numW, baseline, unitPaint)
    return FieldBitmaps(number, unit)
}

/**
 *   27.4 km/h   --tap-->   17.0 mph
 */
@Composable
fun DualUnitView(
    context: Context,
    typeId: String,
    reading: DualReading,
    measure: Measure,
    config: ViewConfig,
    imperial: Boolean,
) {
    val value = if (imperial) reading.imperial else reading.metric
    val unit = if (imperial) measure.imperialUnit else measure.metricUnit
    val bitmaps = renderField(context, config, value, unit)
    // Imperial is shown in the accent colour so it's obvious which unit you're looking at.
    val numberColor = ColorProvider(if (imperial) R.color.imperial_text else R.color.primary_text)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionSendBroadcast(UnitToggle.tapIntent(context, typeId))),
    ) {
        Image(
            provider = ImageProvider(bitmaps.number),
            contentDescription = "$value $unit",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(numberColor),
            modifier = GlanceModifier.fillMaxSize(),
        )
        Image(
            provider = ImageProvider(bitmaps.unit),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(ColorProvider(R.color.secondary_text)),
            modifier = GlanceModifier.fillMaxSize(),
        )
    }
}
