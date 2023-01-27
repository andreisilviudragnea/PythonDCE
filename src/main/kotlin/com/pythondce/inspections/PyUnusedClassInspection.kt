package com.pythondce.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.PyNames
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyClass
import com.pythondce.inspections.quickfix.PyRemoveClassQuickFix
import com.pythondce.util.PythonDCEBundle
import java.util.Locale
import javax.swing.JComponent

private fun PyClass.hasAtLeastOneUsage() =
    hasOneUsage() || findMethodByName(PyNames.INIT, false, null)?.hasOneUsage() == true

class PyUnusedClassInspection : PyInspection() {
    var ignoreTestClasses = true

    override fun getDisplayName(): String = PythonDCEBundle.message("INSP.NAME.unused.class")

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor =
        object : PyInspectionVisitor(holder, getContext(session)) {
            override fun visitPyClass(node: PyClass) {
                val name = node.name ?: return
                if (ignoreTestClasses && name.lowercase(Locale.getDefault()).contains("test")) {
                    return
                }
                if (node.hasAtLeastOneUsage()) {
                    return
                }
                holder.registerProblem(
                    node.nameIdentifier ?: return,
                    PythonDCEBundle.message("INSP.unused.class", name),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    PyRemoveClassQuickFix()
                )
            }
        }

    override fun createOptionsPanel(): JComponent {
        val panel = MultipleCheckboxOptionsPanel(this)
        panel.addCheckbox("Ignore test classes", "ignoreTestClasses")
        return panel
    }
}
