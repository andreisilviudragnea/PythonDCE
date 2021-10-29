package com.pythondce.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.PyNames
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.search.PySuperMethodsSearch
import com.jetbrains.python.psi.types.TypeEvalContext
import com.pythondce.inspections.quickfix.PyRemoveFunctionQuickFix
import com.pythondce.util.PythonDCEBundle
import java.util.*
import javax.swing.JComponent

private fun PyFunction.hasAtLeastOneUsage(): Boolean = hasOneUsage() ||
    PySuperMethodsSearch.search(this, true, TypeEvalContext.userInitiated(project, null)).findAll()
        .any { it is PyFunction && it.hasOneUsage() }

class PyUnusedFunctionInspection : PyInspection() {
    var ignoreTestFunctions = true

    override fun getDisplayName(): String = PythonDCEBundle.message("INSP.NAME.unused.function")

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor =
        object : PyInspectionVisitor(holder, session) {
            override fun visitPyFunction(node: PyFunction) {
                val name = node.name ?: return
                if (ignoreTestFunctions && name.lowercase(Locale.getDefault()).contains("test")) {
                    return
                }
                if (PyNames.PY36_BUILTIN_METHODS.containsKey(name)) {
                    return
                }
                if (node.hasAtLeastOneUsage()) {
                    return
                }
                holder.registerProblem(
                    node.nameIdentifier ?: return,
                    PythonDCEBundle.message("INSP.unused.function", name),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    PyRemoveFunctionQuickFix()
                )
            }
        }

    override fun createOptionsPanel(): JComponent {
        val panel = MultipleCheckboxOptionsPanel(this)
        panel.addCheckbox("Ignore test functions", "ignoreTestFunctions")
        return panel
    }
}
