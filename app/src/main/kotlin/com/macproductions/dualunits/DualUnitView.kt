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
import androidx.compose.ui.unit.dp
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.padding
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider
import io.hammerhead.karooext.models.ViewConfig
import kotlin.math.max
import kotlin.math.ceil

// Side padding (px).
private const val PAD_PX = 8f

private const val UNIT_RATIO = 0.32f // unit label font vs number font

/**
 * Number and unit drawn as two alpha masks, cropped tightly around the text and
 * shown at true pixel size. They're tinted by the Karoo when shown, so theme
 * colours still apply.
 *
 * We draw the text ourselves rather than using a text view because Android pads
 * text vertically, and at the Karoo's standard size that padding pushes the
 * digits out of the field. Cropping tightly means we don't need to know how much
 * of the slot the Karoo's title row takes (it varies by slot size); if the text
 * is ever taller than the space left, the Karoo scales it down to fit.
 */
class FieldBitmaps(val number: Bitmap, val unit: Bitmap)

fun renderField(context: Context, config: ViewConfig, value: String, unitLabel: String): FieldBitmaps {
    val density = context.resources.displayMetrics.density
    val maxW = config.viewSize.first - 2 * PAD_PX

    val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT // regular weight, matching the Karoo's built-in fields
        textSize = config.textSize * density // Karoo's standard size for this slot
    }
    val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT }
    val unitText = " $unitLabel"
    unitPaint.textSize = numPaint.textSize * UNIT_RATIO

    // Shrink only if "value unit" is too wide for the slot.
    val totalW = numPaint.measureText(value) + unitPaint.measureText(unitText)
    if (totalW > maxW) {
        numPaint.textSize *= maxW / totalW
        unitPaint.textSize = numPaint.textSize * UNIT_RATIO
    }

    // Crop from the top of the digits to the bottom of any descender in the unit (e.g. "p" in mph).
    val digit = Rect().also { numPaint.getTextBounds("0", 0, 1, it) }
    val top = digit.top.toFloat() // negative: distance above baseline
    val bottom = unitPaint.fontMetrics.descent
    val numW = numPaint.measureText(value)
    val w = max(1, ceil(numW + unitPaint.measureText(unitText)).toInt())
    val h = max(1, ceil(bottom - top).toInt())
    val baseline = -top

    val number = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
    Canvas(number).drawText(value, 0f, baseline, numPaint)
    val unit = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
    Canvas(unit).drawText(unitText, numW, baseline, unitPaint)
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

    val horizontal = when (config.alignment) {
        ViewConfig.Alignment.LEFT -> Alignment.Horizontal.Start
        ViewConfig.Alignment.CENTER -> Alignment.Horizontal.CenterHorizontally
        ViewConfig.Alignment.RIGHT -> Alignment.Horizontal.End
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
            .clickable(actionSendBroadcast(UnitToggle.tapIntent(context, typeId))),
        contentAlignment = Alignment(horizontal, Alignment.Vertical.CenterVertically),
    ) {
        Image(
            provider = ImageProvider(bitmaps.number),
            contentDescription = "$value $unit",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(numberColor),
        )
        Image(
            provider = ImageProvider(bitmaps.unit),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(ColorProvider(R.color.secondary_text)),
        )
    }
}
