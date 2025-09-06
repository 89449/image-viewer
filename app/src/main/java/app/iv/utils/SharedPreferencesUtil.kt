package app.iv.utils

import android.content.Context
import app.iv.data.MediaType

private const val PREFS_NAME = "filter_prefs"
private const val FILTER_PREF_KEY = "media_filter_preference"

object SharedPreferencesUtil {

    fun saveFilter(context: Context, mediaType: MediaType) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(FILTER_PREF_KEY, mediaType.name)
            .apply()
    }

    fun loadFilter(context: Context): MediaType {
        val savedFilter = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(FILTER_PREF_KEY, MediaType.ALL.name)
        return MediaType.valueOf(savedFilter ?: MediaType.ALL.name)
    }
}
