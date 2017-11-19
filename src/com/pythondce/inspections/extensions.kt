package com.pythondce.inspections

import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch

internal fun PsiElement.numberOfUsages() =
    ReferencesSearch.search(this, GlobalSearchScope.projectScope(project), true).findAll().size

internal fun PsiElement.hasOneUsage() = numberOfUsages() > 0