package com.pythondce.inspections

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyStatementList
import com.jetbrains.python.psi.PyTargetExpression

internal fun PsiElement.usages(): Collection<PsiReference> = ReferencesSearch.search(this).findAll()

internal fun PyTargetExpression.isClassAssignment(): Boolean =
    parent is PyAssignmentStatement &&
        parent.parent is PyStatementList && parent.parent.parent is PyClass

internal fun PsiReference.isUnqualifiedReferenceExpressionInMethodBlock(): Boolean {
    val referenceExpression = element as? PyReferenceExpression ?: return false
    if (referenceExpression.isQualified) return false
    return ScopeUtil.getScopeOwner(referenceExpression) is PyFunction
}

internal fun PyTargetExpression.filterOutUnqualifiedReferencesToNameDefinedInClassBlockFromMethodBlocks(
    references: Collection<PsiReference>,
): Collection<PsiReference> {
    if (!isClassAssignment()) {
        return references
    }
    return references.filterNot { it.isUnqualifiedReferenceExpressionInMethodBlock() }
}

internal fun PyTargetExpression.usages(): Collection<PsiReference> {
    return filterOutUnqualifiedReferencesToNameDefinedInClassBlockFromMethodBlocks((this as PsiElement).usages())
}

internal fun PsiElement.numberOfUsages() = usages().size

internal fun PsiElement.hasOneUsage() = numberOfUsages() > 0
