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
import org.jetbrains.kotlin.lombok.utils.decapitalize
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScClassParameter
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScClass
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import scala.jdk.CollectionConverters

class JsonCodecInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor = object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            if (element is ScClass) {
                val annotation =
                    element.annotations.find { it.hasQualifiedName("io.circe.generic.JsonCodec") } ?: return

                val parameters = CollectionConverters.SeqHasAsJava(element.parameters()).asJava()

                if (parameters.size > 22) {
                    return
                }

                holder.registerProblem(
                    element.nameIdentifier ?: return, // TODO: Fix isPhysical false
                    "JsonCodec",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    object : LocalQuickFix {
                        override fun getFamilyName(): String = "JsonCodec"

                        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                            val name = element.name!!

                            val tuple = if (parameters.size == 1) {
                                "v." + parameters[0].name()
                            } else {
                                parameters.joinToString(
                                    prefix = "(",
                                    postfix = ")"
                                ) { "v." + it.name() }
                            }

                            fun ScClassParameter.specialName(): String {
                                val name1 = name()
                                if (name1.startsWith("`")) {
                                    return name1.substring(1, name1.length - 1)
                                }
                                return name1
                            }

                            val arguments = parameters.joinToString { "\"${it.specialName()}\"" }

                            val companion = if (element.typeParameters.size > 0) {
                                val encoderTypeParams = element.typeParameters.joinToString(
                                    prefix = "[",
                                    postfix = "]"
                                ) { "${it.name}: io.circe.Encoder" }
                                val decoderTypeParams = element.typeParameters.joinToString(
                                    prefix = "[",
                                    postfix = "]"
                                ) { "${it.name}: io.circe.Decoder" }
                                val typeParamsCall =
                                    element.typeParameters.joinToString(prefix = "[", postfix = "]") { it.name!! }

                                if (annotation.findAttributeValue("decodeOnly") != null) {
                                    ScalaPsiElementFactory.createObjectWithContext(
                                        "object $name {\n" +
                                            "implicit def ${name.decapitalize()}Decoder$decoderTypeParams: io.circe.Decoder[$name$typeParamsCall] = io.circe.Decoder.forProduct${parameters.size}($arguments)($name$typeParamsCall)\n" +
                                            "}",
                                        element.context,
                                        element
                                    )
                                } else {
                                    ScalaPsiElementFactory.createObjectWithContext(
                                        "object $name {\n" +
                                            "implicit def ${name.decapitalize()}Encoder$encoderTypeParams: io.circe.Encoder.AsObject[$name$typeParamsCall] = io.circe.Encoder.forProduct${parameters.size}($arguments)(v => $tuple)\n" +
                                            "implicit def ${name.decapitalize()}Decoder$decoderTypeParams: io.circe.Decoder[$name$typeParamsCall] = io.circe.Decoder.forProduct${parameters.size}($arguments)($name$typeParamsCall)\n" +
                                            "}",
                                        element.context,
                                        element
                                    )
                                }
                            } else {
                                if (annotation.findAttributeValue("decodeOnly") != null) {
                                    ScalaPsiElementFactory.createObjectWithContext(
                                        "object $name {\n" +
                                            "implicit val ${name.decapitalize()}Decoder: io.circe.Decoder[$name] = io.circe.Decoder.forProduct${parameters.size}($arguments)($name.apply)\n" +
                                            "}",
                                        element.context,
                                        element
                                    )
                                } else {
                                    ScalaPsiElementFactory.createObjectWithContext(
                                        "object $name {\n" +
                                            "implicit val ${name.decapitalize()}Encoder: io.circe.Encoder.AsObject[$name] = io.circe.Encoder.forProduct${parameters.size}($arguments)(v => $tuple)\n" +
                                            "implicit val ${name.decapitalize()}Decoder: io.circe.Decoder[$name] = io.circe.Decoder.forProduct${parameters.size}($arguments)($name.apply)\n" +
                                            "}",
                                        element.context,
                                        element
                                    )
                                }
                            }

                            val baseCompanion = element.baseCompanion()
                            if (baseCompanion.isEmpty) {
                                element.parent.addAfter(companion, element)
                            } else {
                                val get = baseCompanion.get()
                                CollectionConverters.SeqHasAsJava(companion.members()).asJava().reversed().forEach {
                                    get.addBefore(
                                        it,
                                        CollectionConverters.SeqHasAsJava(get.members()).asJava().first()
                                    )
                                }
                            }

                            annotation.delete()
                        }
                    }
                )
            }
        }
    }
}
