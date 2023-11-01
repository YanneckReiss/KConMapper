package de.yanneckreiss.kconmapper.processor.visitor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import de.yanneckreiss.kconmapper.processor.*
import de.yanneckreiss.kconmapper.processor.KCMConstants.GENERATED_FILE_PATH
import de.yanneckreiss.kconmapper.processor.common.KConMapperConfiguration
import de.yanneckreiss.kconmapper.processor.generator.MappingFunctionGenerator
import java.io.OutputStream

private fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

private const val KCONMAPPER_FROM_CLASSES_ANNOTATION_ARG_NAME = "fromClasses"
private const val KCONMAPPER_TARGET_CLASSES_ANNOTATION_ARG_NAME = "toClasses"
private const val GENERATED_CLASS_SUFFIX = "KConMapperExtensions"
private const val SUPPRESS_UNCHECKED_CAST_STATEMENT = "@file:Suppress(\"UNCHECKED_CAST\")\n\n"
private const val PACKAGE_STATEMENT = "package"

/**
 * Iterates over the [KConMapper] annotated classes and generates extension functions that
 * automatically handle the mapping between the:
 *
 * `targetClass` = The class we want to map to via the generated mapping function.
 * `sourceClass` = Class that receives the generated extension function.
 *
 * Generated functions look like the following:
 *      ```
 *          SourceClass.toTargetClass(): TargetClass
 *      ```
 */
class KCMVisitor(
    private val codeGenerator: CodeGenerator,
    private val resolver: Resolver,
    private val logger: KSPLogger,
    private val configuration: KConMapperConfiguration,
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val annotatedClass: KSClassDeclaration = classDeclaration
        val kcmAnnotation: KSAnnotation = extractKCMAnnotation(annotatedClass)
        val mapFromClasses: List<KSClassDeclaration> = extractArgumentClasses(kcmAnnotation, KCONMAPPER_FROM_CLASSES_ANNOTATION_ARG_NAME)
        val mapToClasses: List<KSClassDeclaration> = extractArgumentClasses(kcmAnnotation, KCONMAPPER_TARGET_CLASSES_ANNOTATION_ARG_NAME)

        // Nothing to do if none of the mapping arguments is filled
        if (mapFromClasses.isEmpty() && mapToClasses.isEmpty()) {
            logger.warn("Missing mapping functions for @$KCONMAPPER_ANNOTATION_NAME annotated class $annotatedClass.")
            return
        }

        val mappingFunctionGenerator = MappingFunctionGenerator(
            resolver = resolver,
            logger = logger
        )

        var extensionFunctions = ""
        val packageImports = PackageImports()

        if (mapFromClasses.isNotEmpty()) {
            mapFromClasses.forEach { sourceClass: KSClassDeclaration ->
                extensionFunctions += mappingFunctionGenerator.generateMappingFunction(
                    targetClass = annotatedClass,
                    sourceClass = sourceClass,
                    packageImports = packageImports,
                    configuration = configuration
                )
            }
        }

        if (mapToClasses.isNotEmpty()) {
            mapToClasses.forEach { targetClass: KSClassDeclaration ->
                extensionFunctions += mappingFunctionGenerator.generateMappingFunction(
                    targetClass = targetClass,
                    sourceClass = annotatedClass,
                    packageImports = packageImports,
                    configuration = configuration
                )
            }
        }

        generateCode(
            containingFile = classDeclaration.containingFile!!,
            targetClassName = annotatedClass.simpleName.getShortName(),
            packageImports = packageImports,
            extensionFunctions = extensionFunctions
        )
    }

    override fun visitAnnotation(annotation: KSAnnotation, data: Unit) {
        annotation.annotationType.resolve().declaration.accept(this, data)
    }

    private fun generateCode(
        containingFile: KSFile,
        targetClassName: String,
        packageImports: PackageImports,
        extensionFunctions: String
    ) {
        // Write the actual Kotlin File that contains the generated extension functions
        codeGenerator.createNewFile(
            dependencies = Dependencies(true, containingFile),
            packageName = GENERATED_FILE_PATH,
            fileName = "${targetClassName}$GENERATED_CLASS_SUFFIX"
        ).use { generatedFileOutputStream: OutputStream ->
            // TODO: Only add the suppression line if at least one type cast occurred.
            if (packageImports.targetClassTypeParameters.isNotEmpty()) generatedFileOutputStream.appendText(SUPPRESS_UNCHECKED_CAST_STATEMENT)
            generatedFileOutputStream.appendText("$PACKAGE_STATEMENT $GENERATED_FILE_PATH\n\n")
            generatedFileOutputStream.appendText(packageImports.asFormattedImports())
            generatedFileOutputStream.appendText(extensionFunctions)
        }
    }

    private fun extractKCMAnnotation(targetClass: KSClassDeclaration): KSAnnotation {

        // Checks if the class is annotated with the [KConMapper] annotation
        val kcmAnnotation: KSAnnotation = targetClass.annotations
            .first { targetClassAnnotations -> targetClassAnnotations.shortName.asString() == KCONMAPPER_ANNOTATION_NAME }

        // Checks if the class that pretends to be the [KConMapper] annotation has the `classes` argument
        kcmAnnotation.arguments.firstOrNull { constructorParam ->
            constructorParam.name?.asString() == KCONMAPPER_FROM_CLASSES_ANNOTATION_ARG_NAME
        } ?: run {
            logger.logAndThrowError(
                errorMessage = "Classes annotated with `@$KCONMAPPER_ANNOTATION_NAME` must contain " +
                        "at least one class as a parameter like: `$KCONMAPPER_ANNOTATION_NAME(classes = [YourClassToMap::kt])",
                targetClass = targetClass
            )
        }

        return kcmAnnotation
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractArgumentClasses(kcmAnnotation: KSAnnotation, paramName: String): List<KSClassDeclaration> {
        return kcmAnnotation
            .arguments
            .find { annotationArgument: KSValueArgument -> annotationArgument.name?.asString() == paramName }
            ?.let { ksValueArgument -> ksValueArgument.value as List<KSType> }
            ?.mapNotNull { argumentClassType -> resolver.getClassDeclarationByName(argumentClassType.declaration.qualifiedName!!) } // TODO: Check if !! is okay here
            ?: emptyList()
    }
}
