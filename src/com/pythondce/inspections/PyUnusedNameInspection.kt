package com.pythondce.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.jetbrains.python.PyNames
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.inspections.quickfix.PyRemoveStatementQuickFix
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyStatementList
import com.jetbrains.python.psi.PyTargetExpression

private fun PyTargetExpression.isGlobalAssignment(): Boolean = parent is PyAssignmentStatement
    && parent.parent is PsiFile

private fun PyTargetExpression.isClassAssignment(): Boolean = parent is PyAssignmentStatement
    && parent.parent is PyStatementList && parent.parent.parent is PyClass

class PyUnusedNameInspection : PyInspection() {
  override fun getDisplayName(): String = PythonDCEBundle.message("INSP.NAME.unused.name")

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
      object : PyInspectionVisitor(holder, session) {
        override fun visitPyTargetExpression(node: PyTargetExpression?) {
          node ?: return
          if (PyNames.SLOTS != node.name && (node.isGlobalAssignment() || node.isClassAssignment())
              && node.numberOfUsages() == 1) {
            holder.registerProblem(node.nameIdentifier ?: return,
                PythonDCEBundle.message("INSP.unused.name"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                PyRemoveStatementQuickFix())
          }
        }
      }
}