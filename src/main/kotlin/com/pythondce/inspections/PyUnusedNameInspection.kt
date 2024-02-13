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
import com.jetbrains.python.psi.PyTargetExpression
import com.pythondce.util.PythonDCEBundle

private fun PyTargetExpression.isGlobalAssignment(): Boolean =
    parent is PyAssignmentStatement &&
        parent.parent is PsiFile

class PyUnusedNameInspection : PyInspection() {
    override fun getDisplayName(): String = PythonDCEBundle.message("INSP.NAME.unused.name")

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession,
    ): PsiElementVisitor =
        object : PyInspectionVisitor(holder, getContext(session)) {
            override fun visitPyTargetExpression(node: PyTargetExpression) {
                val name = node.name ?: return
                if (PyNames.SLOTS == name || PyNames.ALL == name) {
                    return
                }
                if ((node.isGlobalAssignment() || node.isClassAssignment()) && node.usages().size == 1) {
                    holder.registerProblem(
                        node.nameIdentifier ?: return,
                        PythonDCEBundle.message("INSP.unused.name", name),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        PyRemoveStatementQuickFix(),
                    )
                }
            }
        }
}
