package com.pythondce.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.PyNames
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyClass
import com.pythondce.inspections.quickfix.PyRemoveClassQuickFix

private fun PyClass.hasAtLeastOneUsage() =
    hasOneUsage() || findMethodByName(PyNames.INIT, false, null)?.hasOneUsage() == true

class PyUnusedClassInspection : PyInspection() {
  override fun getDisplayName(): String = PythonDCEBundle.message("INSP.NAME.unused.class")

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
      object : PyInspectionVisitor(holder, session) {
        override fun visitPyClass(node: PyClass?) {
          node ?: return
          if (!node.hasAtLeastOneUsage()) {
            holder.registerProblem(node.nameIdentifier ?: return, PythonDCEBundle.message("INSP.unused.class"),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING, PyRemoveClassQuickFix())
          }
        }
      }
}