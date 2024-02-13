package com.pythondce.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyLiteralExpression
import com.jetbrains.python.psi.types.PyCallableParameter
import com.pythondce.inspections.quickfix.PyParameterToLocalQuickFix
import com.pythondce.util.PythonDCEBundle

private fun PyFunction.calls(): List<PyCallExpression> = usages().map { it.element.parent }.filterIsInstance<PyCallExpression>()

private const val NOT_CONSTANT = "NOT_CONSTANT"

class PySameParameterValueInspection : PyInspection() {
    override fun getDisplayName(): String = PythonDCEBundle.message("inspection.same.parameter.display.name")

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession,
    ): PsiElementVisitor {
        return object : PyInspectionVisitor(holder, getContext(session)) {
            override fun visitPyFunction(node: PyFunction) {
                val parametersToArgumentValues: MutableMap<PyCallableParameter, MutableSet<String>> = HashMap()
                for (call in node.calls()) {
                    call.multiMapArguments(resolveContext)
                        .map { pyArgumentsMapping -> pyArgumentsMapping.mappedParameters.entries }
                        .forEach { entries ->
                            entries.forEach { entry ->
                                parametersToArgumentValues
                                    .getOrPut(entry.value) { HashSet() }
                                    .add(if (entry.key is PyLiteralExpression) entry.key.text else NOT_CONSTANT)
                            }
                        }
                }
                parametersToArgumentValues.entries
                    .filter { entry -> entry.value.size == 1 && !entry.value.contains(NOT_CONSTANT) }
                    .forEach { entry ->
                        val parameterName = entry.key.parameter?.name ?: return
                        val constant = entry.value.elementAt(0)
                        registerProblem(
                            entry.key.parameter,
                            PythonDCEBundle.message(
                                "inspection.same.parameter.problem.descriptor",
                                parameterName,
                                constant,
                            ),
                            PyParameterToLocalQuickFix(parameterName, constant),
                        )
                    }
            }
        }
    }
}
