package com.pythondce.inspections.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyClass
import com.pythondce.util.PythonDCEBundle

class PyRemoveClassQuickFix : LocalQuickFix {
    override fun getFamilyName(): String = PythonDCEBundle.message("QFIX.NAME.remove.class")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        PsiTreeUtil.getParentOfType(descriptor.psiElement, PyClass::class.java, true)?.delete()
    }
}
