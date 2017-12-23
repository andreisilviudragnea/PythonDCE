package com.pythondce.util

import com.intellij.CommonBundle
import org.jetbrains.annotations.PropertyKey
import java.util.*

object PythonDCEBundle {
  private const val BUNDLE = "com.pythondce.strings"
  internal val bundle: ResourceBundle by lazy(LazyThreadSafetyMode.NONE) { ResourceBundle.getBundle(BUNDLE) }

  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String): String = bundle.getString(key)

  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: String): String =
      CommonBundle.message(bundle, key, *params)
}
