package com.macproductions.dualunits

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A graphical data field that reads one built-in Karoo value and shows it in
 * metric; tapping the field switches it to imperial and back.
 */
@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class DualUnitDataType(
    private val karooSystem: KarooSystemService,
    extension: String,
    typeId: String,
    private val sourceType: String,
    private val sourceField: String,
    private val measure: Measure,
    /** Value shown while editing ride pages, in Karoo base units (m/s or m). */
    private val previewValue: Double,
) : DataTypeImpl(extension, typeId) {

    private val glance = GlanceRemoteViews()

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            // Keep Karoo's standard header (icon + field name) above our view.
            emitter.onNext(UpdateGraphicConfig(showHeader = true))

            val raw: Flow<Double?> = if (config.preview) {
                flowOf(previewValue)
            } else {
                karooSystem.streamDataFlow(sourceType).map { state ->
                    (state as? StreamState.Streaming)?.dataPoint?.values?.get(sourceField)
                }
            }

            val readings = raw.map { format(measure, it) }
            // Redraw when the displayed text changes or the field is tapped.
            val imperial = UnitToggle.imperialTypes(context).map { typeId in it }
            combine(readings, imperial) { reading, isImperial -> reading to isImperial }
                .distinctUntilChanged()
                .collect { (reading, isImperial) ->
                    val result = glance.compose(context, DpSize.Unspecified) {
                        DualUnitView(context, typeId, reading, measure, config, isImperial)
                    }
                    emitter.updateView(result.remoteViews)
                }
        }
        emitter.setCancellable {
            Timber.d("Stopping $dataTypeId view")
            job.cancel()
        }
    }
}

fun KarooSystemService.streamDataFlow(dataTypeId: String): Flow<StreamState> = callbackFlow {
    val listenerId = addConsumer(OnStreamState.StartStreaming(dataTypeId)) { event: OnStreamState ->
        trySendBlocking(event.state)
    }
    awaitClose { removeConsumer(listenerId) }
}
