package com.pythondce.util

import com.intellij.AbstractBundle
import org.jetbrains.annotations.PropertyKey
import java.util.ResourceBundle

object PythonDCEBundle {
    private const val BUNDLE = "com.pythondce.strings"
    internal val bundle: ResourceBundle by lazy(LazyThreadSafetyMode.NONE) { ResourceBundle.getBundle(BUNDLE) }

    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
    ): String = bundle.getString(key)

    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: String,
    ): String = AbstractBundle.message(bundle, key, *params)
}
