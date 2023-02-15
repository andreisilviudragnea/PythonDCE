package io.dragnea.usages

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScConstructorOwner
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import scala.jdk.CollectionConverters

class JsonCodecInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor = object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            if (element is ScConstructorOwner) {
                val annotation =
                    element.annotations.find { it.hasQualifiedName("io.circe.generic.JsonCodec") } ?: return

                holder.registerProblem(
                    element.nameIdentifier ?: return, // TODO: Fix isPhysical false
                    "JsonCodec",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    object : LocalQuickFix {
                        override fun getFamilyName(): String = "JsonCodec"

                        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                            annotation.delete()

                            val parameters = CollectionConverters.SeqHasAsJava(element.parameters()).asJava()

                            val name = element.name

                            val tuple = if (parameters.size == 1) {
                                "v." + parameters[0].name
                            } else {
                                parameters.joinToString(
                                    prefix = "(",
                                    postfix = ")"
                                ) { "v." + it.name }
                            }

                            val arguments = parameters.joinToString { "\"${it.name}\"" }

                            val companion = ScalaPsiElementFactory.createObjectWithContext(
                                "object $name {\n implicit val ${name}Codec: io.circe.Codec.AsObject[$name] = io.circe.Codec.forProduct${parameters.size}($arguments)($name.apply)(v => $tuple) \n}",
                                element.context,
                                element
                            )

                            val baseCompanion = element.baseCompanion()
                            if (baseCompanion.isEmpty) {
                                element.parent.addAfter(companion, element)
                            } else {
                                val get = baseCompanion.get()
                                get.addBefore(
                                    CollectionConverters.SeqHasAsJava(companion.members()).asJava().first(),
                                    CollectionConverters.SeqHasAsJava(get.members()).asJava().first()
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
