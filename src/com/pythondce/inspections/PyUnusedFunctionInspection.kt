package com.pythondce.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.PyNames
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.search.PySuperMethodsSearch
import com.jetbrains.python.psi.types.TypeEvalContext
import com.pythondce.inspections.quickfix.PyRemoveFunctionQuickFix

private fun PyFunction.hasAtLeastOneUsage(): Boolean = hasOneUsage() ||
    PySuperMethodsSearch.search(this, true, TypeEvalContext.userInitiated(project, null)).findAll().any { it is PyFunction && it.hasOneUsage() }

class PyUnusedFunctionInspection : PyInspection() {
  override fun getDisplayName(): String = PythonDCEBundle.message("INSP.NAME.unused.function")

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
      object : PyInspectionVisitor(holder, session) {
        override fun visitPyFunction(node: PyFunction?) {
          node ?: return
          val name = node.name ?: return
          if (!PyNames.PY3_BUILTIN_METHODS.containsKey(name) && !node.hasAtLeastOneUsage()) {
            holder.registerProblem(node.nameIdentifier ?: return,
                PythonDCEBundle.message("INSP.unused.function"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                PyRemoveFunctionQuickFix())
          }
        }
      }
}