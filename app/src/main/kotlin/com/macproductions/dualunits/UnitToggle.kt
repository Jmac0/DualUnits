package com.macproductions.dualunits

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Remembers, per data field, whether it is currently showing imperial (true) or metric (false).
 * Saved so the choice survives the Karoo restarting the extension.
 */
object UnitToggle {
    private const val PREFS = "unit_toggle"
    private val state = MutableStateFlow<Set<String>>(emptySet()) // typeIds showing imperial
    private var loaded = false

    fun imperialTypes(context: Context): StateFlow<Set<String>> {
        synchronized(this) {
            if (!loaded) {
                state.value = prefs(context).getStringSet(PREFS, emptySet())!!.toSet()
                loaded = true
            }
        }
        return state.asStateFlow()
    }

    fun toggle(context: Context, typeId: String) {
        imperialTypes(context)
        state.update { if (typeId in it) it - typeId else it + typeId }
        prefs(context).edit().putStringSet(PREFS, state.value).apply()
        Log.i("DualUnits", "toggled $typeId -> imperial=${typeId in state.value}")
    }

    /** Intent fired when a field is tapped. The data URI keeps each field's tap distinct. */
    fun tapIntent(context: Context, typeId: String): Intent =
        Intent(context, ToggleReceiver::class.java)
            .setAction(ToggleReceiver.ACTION)
            .setData(Uri.parse("dualunits://toggle/$typeId"))

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

class ToggleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val typeId = intent.data?.lastPathSegment ?: return
        UnitToggle.toggle(context, typeId)
    }

    companion object {
        const val ACTION = "com.macproductions.dualunits.TOGGLE"
    }
}
