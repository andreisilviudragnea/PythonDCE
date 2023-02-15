/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pythondce.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.PyNames
import com.jetbrains.python.codeInsight.intentions.PyBaseIntentionAction
import com.jetbrains.python.documentation.PythonDocumentationProvider
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyUtil
import com.jetbrains.python.psi.types.TypeEvalContext
import com.pythondce.util.PythonDCEBundle

class PyGenerateInitIntention : PyBaseIntentionAction() {
    override fun getFamilyName(): String = PythonDCEBundle.message("INTN.generate.init")

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        if (file !is PyFile) {
            return false
        }
        val element = file.findElementAt(editor.caretModel.offset)
        val pyClass = PsiTreeUtil.getParentOfType(element, PyClass::class.java, false) ?: return false
        if (PyUtil.getInitMethod(pyClass) !== null) {
            return false
        }
        text = PythonDCEBundle.message("INTN.generate.init")
        return true
    }

    override fun doInvoke(project: Project, editor: Editor, file: PsiFile) {
        val pyClass = PsiTreeUtil.getParentOfType(
            file.findElementAt(editor.caretModel.offset),
            PyClass::class.java,
            false
        ) ?: return
        val context = TypeEvalContext.userInitiated(file.project, file)
        val classAttributesNamesAndTypes = pyClass.classAttributes.map {
            Pair(it.name ?: return, PythonDocumentationProvider.getTypeName(context.getType(it), context))
        }
        val parameters = mutableListOf(PyNames.CANONICAL_SELF)
        parameters.addAll(classAttributesNamesAndTypes.map { "${it.first}: ${it.second}" })
        val methodRows = mutableListOf("def ${PyNames.INIT}(${parameters.joinToString()}):")
        methodRows.addAll(classAttributesNamesAndTypes.map { "${PyNames.CANONICAL_SELF}.${it.first}: ${it.second} = ${it.first}" })
        val initMethod = PyElementGenerator.getInstance(project).createFromText(
            LanguageLevel.PYTHON36,
            PyFunction::class.java,
            methodRows.joinToString(separator = "\n    ")
        )
        pyClass.statementList.add(initMethod)
    }
}
