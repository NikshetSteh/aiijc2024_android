package ru.naviai.aiijc

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.view.ContextThemeWrapper
import ru.NaviAI.aiijc.R
import java.util.*

class ApplicationLanguageHelper(base: Context) :
    ContextThemeWrapper(base, R.style.Theme_aiijc2024) {
    companion object {

        fun wrap(context: Context, language: String): ContextThemeWrapper {
            var newContext = context
            val config = newContext.resources.configuration
            if (language != "") {
                val locale = Locale(language)
                Locale.setDefault(locale)
                setSystemLocale(config, locale)
                config.setLayoutDirection(locale)
                newContext = newContext.createConfigurationContext(config)
            }
            return ApplicationLanguageHelper(newContext)
        }


        fun setSystemLocale(config: Configuration, locale: Locale) {
            config.setLocale(locale)
        }
    }
}