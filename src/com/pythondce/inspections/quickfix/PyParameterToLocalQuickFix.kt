package com.pythondce.inspections.quickfix

import com.intellij.CommonBundle
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.*
import com.pythondce.util.PythonDCEBundle

class PyParameterToLocalQuickFix(private val parameterName: String, private val constant: String) : LocalQuickFix {
  override fun getFamilyName(): String = PythonDCEBundle.message("inspection.same.parameter.fix.family.name")

  override fun getName(): String {
    return CommonBundle.message(PythonDCEBundle.bundle, "inspection.same.parameter.fix.name", parameterName, constant)
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val pyParameter = descriptor.psiElement as? PyParameter ?: return
    val statementsList = (pyParameter.parent.parent as PyFunction).statementList
    val assignment = PyElementGenerator
        .getInstance(project)
        .createFromText(LanguageLevel.PYTHON36, PyAssignmentStatement::class.java, "$parameterName = $constant")
    PyUtil.addElementToStatementList(assignment, statementsList, true)
  }
}