package com.pythondce.inspections.quickfix

import com.intellij.AbstractBundle
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyParameter
import com.jetbrains.python.refactoring.PyPsiRefactoringUtil.addElementToStatementList
import com.pythondce.util.PythonDCEBundle

class PyParameterToLocalQuickFix(private val parameterName: String, private val constant: String) : LocalQuickFix {
    override fun getFamilyName(): String = PythonDCEBundle.message("inspection.same.parameter.fix.family.name")

    override fun getName(): String {
        return AbstractBundle.message(
            PythonDCEBundle.bundle,
            "inspection.same.parameter.fix.name",
            parameterName,
            constant,
        )
    }

    override fun applyFix(
        project: Project,
        descriptor: ProblemDescriptor,
    ) {
        val pyParameter = descriptor.psiElement as? PyParameter ?: return
        val statementsList = (pyParameter.parent.parent as PyFunction).statementList
        val assignment =
            PyElementGenerator
                .getInstance(project)
                .createFromText(LanguageLevel.PYTHON36, PyAssignmentStatement::class.java, "$parameterName = $constant")
        addElementToStatementList(assignment, statementsList, true)
    }
}
