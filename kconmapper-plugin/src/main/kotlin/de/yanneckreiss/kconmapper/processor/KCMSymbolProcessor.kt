package de.yanneckreiss.kconmapper.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import de.yanneckreiss.kconmapper.processor.visitor.KCMVisitor

private const val KCONMAPPER_PACKAGE_NAME = "de.yanneckreiss.kconmapper"
private const val KCONMAPPER_ANNOTATIONS_PACKAGE_NAME = "annotations"
private const val KCONMAPPER_ANNOTATION_NAME = "KConMapper"

class KCMSymbolProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotationPackagePath = "$KCONMAPPER_PACKAGE_NAME.$KCONMAPPER_ANNOTATIONS_PACKAGE_NAME.$KCONMAPPER_ANNOTATION_NAME"
        val symbols: Sequence<KSAnnotated> = resolver.getSymbolsWithAnnotation(annotationName = annotationPackagePath)
        val ret: List<KSAnnotated> = symbols.filter { ksAnnotated -> !ksAnnotated.validate() }.toList()

        symbols
            .filter { ksAnnotated -> ksAnnotated is KSClassDeclaration && ksAnnotated.validate() }
            .forEach { ksAnnotated: KSAnnotated ->
                val classDeclaration: KSClassDeclaration = (ksAnnotated as KSClassDeclaration)
                when (classDeclaration.classKind) {
                    ClassKind.INTERFACE,
                    ClassKind.ENUM_CLASS,
                    ClassKind.ENUM_ENTRY,
                    ClassKind.OBJECT,
                    ClassKind.ANNOTATION_CLASS -> {
                        logger.logAndThrowError(
                            errorMessage = "Cannot generate function for class `${classDeclaration.getName()}`, " +
                                    "class type `${classDeclaration.classKind}` is not supported.",
                            targetClass = classDeclaration
                        )
                    }
                    else -> {
                        // Class type is supported
                        ksAnnotated.accept(KCMVisitor(codeGenerator, resolver, logger), Unit)
                    }
                }
            }

        return ret
    }
}