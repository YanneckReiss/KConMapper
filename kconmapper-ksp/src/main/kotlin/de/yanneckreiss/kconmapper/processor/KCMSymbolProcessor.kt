package de.yanneckreiss.kconmapper.processor

import com.github.yanneckreiss.kconmapper.annotations.KConMapper
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import de.yanneckreiss.kconmapper.processor.common.KConMapperConfiguration
import de.yanneckreiss.kconmapper.processor.visitor.KCMVisitor

private const val KCONMAPPER_PACKAGE_NAME = "com.github.yanneckreiss.kconmapper"
private const val KCONMAPPER_ANNOTATIONS_PACKAGE_NAME = "annotations"
const val KCONMAPPER_ANNOTATION_NAME = "KConMapper"

/**
 * Responsible for finding the KConMapper annotations
 */
class KCMSymbolProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    val kConMapperConfiguration: KConMapperConfiguration
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val resolvedSymbols: Sequence<KSAnnotated> = resolver.getSymbolsWithAnnotation(annotationName = KConMapper::class.qualifiedName!!)

        resolvedSymbols
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
                        val kcmVisitor = KCMVisitor(
                            codeGenerator = codeGenerator,
                            resolver = resolver,
                            logger = logger,
                            configuration = kConMapperConfiguration,
                        )
                        // Class type is supported
                        ksAnnotated.accept(
                            visitor = kcmVisitor,
                            data = Unit
                        )
                    }
                }
            }

        return emptyList()
    }
}