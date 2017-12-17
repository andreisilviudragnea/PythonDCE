package com.pythondce.inspections

import org.jetbrains.annotations.PropertyKey
import java.util.*

object PythonDCEBundle {
  private const val BUNDLE = "com.pythondce.strings"
  internal val bundle: ResourceBundle by lazy(LazyThreadSafetyMode.NONE) { ResourceBundle.getBundle(BUNDLE) }

  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String): String = bundle.getString(key)
}
