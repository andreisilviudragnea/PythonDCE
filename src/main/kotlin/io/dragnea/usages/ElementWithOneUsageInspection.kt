package io.dragnea.usages

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiNamedElement
import com.pythondce.inspections.usages
import org.jetbrains.plugins.scala.lang.psi.impl.base.patterns.ScPatternArgumentListImpl
import org.jetbrains.plugins.scala.lang.psi.impl.base.patterns.ScPatternsImpl
import org.jetbrains.plugins.scala.lang.psi.impl.base.patterns.ScReferencePatternImpl
import org.jetbrains.plugins.scala.lang.psi.impl.expr.ScGeneratorImpl
import org.jetbrains.plugins.scala.lang.psi.impl.statements.ScFunctionDeclarationImpl
import org.jetbrains.plugins.scala.lang.psi.impl.statements.ScFunctionDefinitionImpl
import org.jetbrains.plugins.scala.lang.psi.impl.statements.params.ScParameterImpl
import org.jetbrains.plugins.scala.lang.psi.impl.statements.params.ScTypeParamImpl

class ElementWithOneUsageInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor = object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            element is PsiNamedElement || return

            element::class in listOf(ScParameterImpl::class, ScTypeParamImpl::class) && return

            element is ScReferencePatternImpl && element.parent::class in listOf(
                ScGeneratorImpl::class,
                ScPatternsImpl::class,
                ScPatternArgumentListImpl::class
            ) && return

            if (element.usages().size == 1) {
                holder.registerProblem(
                    when (element) {
                        is ScFunctionDefinitionImpl<*> -> element.nameId()
                        is ScFunctionDeclarationImpl -> element.nameId()
                        else -> element
                    },
                    "Element has only one usage and can be inlined",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                )
            }
        }
    }
}
