package io.dragnea.usages

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiTypeParameter
import com.pythondce.inspections.usages
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScPattern

class ElementWithOneUsageInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor = object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            if (element !is PsiNameIdentifierOwner) return

            element is PsiParameter && return
            element is KtParameter && return // KtParameter is not PsiParameter
            element is PsiTypeParameter && return

            element is ScPattern && return

            if (element.usages().size == 1) {
                holder.registerProblem(
                    element.nameIdentifier!!,
                    "Element has only one usage and can be inlined",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                )
            }
        }
    }
}
