package io.dragnea.usages

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiTypeParameter
import com.pythondce.inspections.usages
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScNamingPattern
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScReferencePattern
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScTypedPattern
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScGenerator
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScArguments
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScTrait
import org.rust.lang.core.psi.RsMatchArm
import org.rust.lang.core.psi.RsPatTup
import org.rust.lang.core.psi.RsPatTupleStruct
import org.rust.lang.core.psi.RsValueParameter

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
            element is PsiClass && return

            element.parent?.parent?.parent is ScTrait && return // Skip methods from traits
            element is ScTypedPattern && return
            element is ScReferencePattern && element.parent is ScGenerator && return
            element is ScReferencePattern && element.parent is ScArguments && return
            element is ScNamingPattern && return

            element.parent?.parent is RsValueParameter && return
            element.parent?.parent is RsPatTupleStruct && return
            element.parent?.parent is RsPatTup && return
            element.parent?.parent is RsMatchArm && return

            if (element.usages().size == 1) {
                holder.registerProblem(
                    element.nameIdentifier ?: return,
                    "Element has only one usage and can be inlined",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                )
            }
        }
    }
}
