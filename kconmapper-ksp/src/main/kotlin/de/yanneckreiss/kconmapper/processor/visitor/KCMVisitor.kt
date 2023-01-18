package de.yanneckreiss.kconmapper.processor.visitor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import de.yanneckreiss.kconmapper.processor.*
import de.yanneckreiss.kconmapper.processor.visitor.argument.ArgumentType
import de.yanneckreiss.kconmapper.processor.visitor.argument.MatchingArgument
import java.io.OutputStream

private fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

private const val KCONMAPPER_FROM_CLASSES_ANNOTATION_ARG_NAME = "fromClasses"
private const val KCONMAPPER_TARGET_CLASSES_ANNOTATION_ARG_NAME = "targetClasses"
private const val KCONMAPPER_ANNOTATION_NAME = "KConMapper"
private const val KCONMAPPER_PROPERTY_ANNOTATION_NAME = "KConMapperProperty"
private const val GENERATED_CLASS_SUFFIX = "KConMapperExtensions"
private const val DIAMOND_OPERATOR_OPEN = "<"
private const val DIAMOND_OPERATOR_CLOSE = ">"
private const val KOTLIN_FUNCTION_KEYWORD = "fun"
private const val CLOSE_FUNCTION = ")"
private const val OPEN_FUNCTION = "("
private const val SUPPRESS_UNCHECKED_CAST_STATEMENT = "@file:Suppress(\"UNCHECKED_CAST\")\n\n"
private const val PACKAGE_STATEMENT = "package"
private const val GENERATED_FILE_PATH = "de.yanneckreiss.kconmapper.generated"

/**
 * Iterates over the [de.yanneckreiss.kconmapper.annotations.KConMapper] annotated classes and generates extension function that
 * automatically handle the mapping between
 *
 * `targetClass` = The class we want to map to via generated extension functions.
 * `originClasses` = Classes we want to generate extension functions.
 *
 * Generated functions look like the following:
 *      `originClass.toTargetClass()`
 */
class KCMVisitor(
    private val codeGenerator: CodeGenerator,
    private val resolver: Resolver,
    private val logger: KSPLogger
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        classDeclaration.primaryConstructor!!.accept(this, data)
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        val targetClass: KSClassDeclaration = function.parentDeclaration as KSClassDeclaration
        val targetClassName = targetClass.simpleName.getShortName()
        val packageName = targetClass.containingFile!!.packageName.asString()
        val targetClassTypeParameters: List<KSTypeParameter> = targetClass.typeParameters

        val packageImports = PackageImports(targetClassTypeParameters)

        // Add import for the target class = class we want to map to via extension functions
        packageImports.addImport(packageName, targetClassName)

        // Origin classes = Classes we want to generate extension functions for
        val kcmAnnotation: KSAnnotation = extractKCMAnnotation(targetClass)
        val originClassesArgs: List<KSType> = extractOriginClasses(kcmAnnotation)

        // Add imports for all origin classes
        originClassesArgs.forEach { originClass: KSType ->
            packageImports.addImport(originClass)
        }

        var extensionFunctions = ""

        // Create mapping extension functions for all origin classes
        originClassesArgs.forEachIndexed { originClassIndex: Int, originClass: KSType ->
            extensionFunctions += extractExtensionMapperFunctionForOriginClass(
                originClass = originClass,
                targetClass = targetClass,
                targetClassTypeParameters = targetClassTypeParameters,
                targetClassName = targetClassName,
                packageImports = packageImports,
                isLastOriginClassForTargetClass = originClassIndex != originClassesArgs.lastIndex
            )
        }

        // Generate the actual Kotlin File that contains the Extension functions
        codeGenerator.createNewFile(
            dependencies = Dependencies(true, function.containingFile!!),
            packageName = GENERATED_FILE_PATH,
            fileName = "${targetClassName}$GENERATED_CLASS_SUFFIX"
        ).use { generatedFileOutputStream: OutputStream ->
            // TODO: Only add the suppression line if at least one type cast occurred.
            if (targetClassTypeParameters.isNotEmpty()) generatedFileOutputStream.appendText(SUPPRESS_UNCHECKED_CAST_STATEMENT)
            generatedFileOutputStream.appendText("$PACKAGE_STATEMENT $GENERATED_FILE_PATH\n\n")
            generatedFileOutputStream.appendText(packageImports.asFormattedImports())
            generatedFileOutputStream.appendText(extensionFunctions)
        }
    }

    private fun extractExtensionMapperFunctionForOriginClass(
        originClass: KSType,
        targetClass: KSClassDeclaration,
        targetClassTypeParameters: List<KSTypeParameter>,
        targetClassName: String,
        packageImports: PackageImports,
        isLastOriginClassForTargetClass: Boolean
    ): String {

        var extensionFunctions = ""

        // Create mapping extension functions for all origin classes
        val originClassName: String = originClass.toString()

        // Find all matching arguments from the origin class that can be mapped to the target class
        val (
            missingConstructorArguments: List<KSValueParameter>,
            matchingConstructorArguments: List<MatchingArgument>
        ) = extractMatchingAndMissingConstructorArguments(
            targetClass = targetClass,
            originClass = originClass,
            targetClassTypeParameters = targetClassTypeParameters,
            packageImports = packageImports,
            originClassName = originClassName,
            targetClassName = targetClassName
        )

        // Add missing arguments to function head
        if (missingConstructorArguments.isNotEmpty()) {

            // Warn the user if unnecessary KConMapper got declared
            if (matchingConstructorArguments.isEmpty()) {
                logger.warn(
                    message = "\n\nNo matching arguments for mapping from class `$originClassName` to `$targetClassName`. \n" +
                            "You can instead remove the argument `$originClassName::class` from the `$KCONMAPPER_ANNOTATION_NAME` annotation in class `$targetClassName` " +
                            "and use its constructor directly.",
                    symbol = targetClass
                )
            }

            // Add type parameters from target class to function head
            if (targetClassTypeParameters.isNotEmpty()) {
                extensionFunctions += "$KOTLIN_FUNCTION_KEYWORD $DIAMOND_OPERATOR_OPEN"
                targetClassTypeParameters.forEachIndexed { index, targetClassTypeParameter: KSTypeParameter ->
                    val separator: String = getArgumentDeclarationLineEnding(hasNextLine = targetClassTypeParameters.lastIndex != index, addSpace = true)

                    extensionFunctions += targetClassTypeParameter.name.asString()
                    // TODO: Handle multiple upper bounds
                    targetClassTypeParameter.bounds.firstOrNull()?.let { upperBound: KSTypeReference ->
                        packageImports.addImport(upperBound.resolve())
                        extensionFunctions += ": $upperBound"
                    }

                    extensionFunctions += separator

                }
                extensionFunctions += "$DIAMOND_OPERATOR_CLOSE ${generateExtensionFunctionName(originClassName, targetClassName)}(\n"
            } else {
                extensionFunctions += "$KOTLIN_FUNCTION_KEYWORD ${generateExtensionFunctionName(originClassName, targetClassName)}(\n"
            }

            missingConstructorArguments.forEachIndexed { missingArgumentIndex: Int, missingArgument: KSValueParameter ->
                extensionFunctions += convertMissingConstructorArgumentToDeclarationText(
                    isLastIndex = missingConstructorArguments.lastIndex == missingArgumentIndex,
                    missingArgument = missingArgument,
                    packageImports = packageImports,
                    targetClass = targetClass
                )
            }

            extensionFunctions += "$CLOSE_FUNCTION = $targetClassName(\n"
        }
        // No missing arguments, we can create the default empty param function head
        else {
            // Add type parameters from target class to function head
            if (targetClassTypeParameters.isNotEmpty()) {
                extensionFunctions += "$KOTLIN_FUNCTION_KEYWORD $DIAMOND_OPERATOR_OPEN"
                targetClassTypeParameters.forEachIndexed { index: Int, targetClassTypeParameter: KSTypeParameter ->
                    val lineEnding: String = getArgumentDeclarationLineEnding(hasNextLine = targetClassTypeParameters.lastIndex != index, addSpace = true)
                    extensionFunctions += targetClassTypeParameter.name.asString() + lineEnding
                }
                extensionFunctions += "$DIAMOND_OPERATOR_CLOSE ${generateExtensionFunctionName(originClassName, targetClassName)}$OPEN_FUNCTION$CLOSE_FUNCTION = $targetClassName$OPEN_FUNCTION\n"
            } else {
                extensionFunctions += "$KOTLIN_FUNCTION_KEYWORD ${generateExtensionFunctionName(originClassName, targetClassName)}$OPEN_FUNCTION$CLOSE_FUNCTION = $targetClassName$OPEN_FUNCTION\n"
            }
        }

        // Assign matching values from origin class to target class constructor
        matchingConstructorArguments.forEachIndexed { index: Int, matchingArgument: MatchingArgument ->
            val lineEnding: String = getArgumentDeclarationLineEnding(hasNextLine = missingConstructorArguments.lastIndex != index || missingConstructorArguments.isNotEmpty())
            extensionFunctions += "\t${matchingArgument.targetClassPropertyName} = this.${matchingArgument.originClassPropertyName}"

            // If origin class is a generic type parameter we need to cast it to avoid compilation errors
            matchingArgument.targetClassPropertyGenericTypeName?.let { targetClassPropertyGenericTypeName: String ->
                extensionFunctions += " as $targetClassPropertyGenericTypeName"
            }

            extensionFunctions += "$lineEnding\n"
        }

        // Assign values from function head to constructor param
        missingConstructorArguments.forEachIndexed { index: Int, paramName: KSValueParameter ->
            val lineEnding: String = getArgumentDeclarationLineEnding(hasNextLine = missingConstructorArguments.lastIndex != index)
            extensionFunctions += "\t$paramName = $paramName$lineEnding\n"
        }

        extensionFunctions += "$CLOSE_FUNCTION\n"
        if (isLastOriginClassForTargetClass) extensionFunctions += "\n"

        return extensionFunctions
    }

    private fun convertMissingConstructorArgumentToDeclarationText(
        isLastIndex: Boolean,
        missingArgument: KSValueParameter,
        packageImports: PackageImports,
        targetClass: KSClassDeclaration
    ): String {

        var missingArgumentDeclarationText = ""
        val argumentTypes: MutableList<ArgumentType> = mutableListOf()
        val missingArgumentType: KSType = missingArgument.type.resolve()

        packageImports.addImport(missingArgumentType)

        // Find all required types for the argument class
        missingArgumentType.arguments.forEach { ksTypeArgument: KSTypeArgument ->
            if (ksTypeArgument.variance == Variance.STAR) {
                argumentTypes.add(ArgumentType.Asterix)
            } else {
                val argumentClass: KSType? = ksTypeArgument.type?.resolve()

                if (argumentClass != null) {
                    argumentTypes.add(ArgumentType.ArgumentClass(argumentClass))
                } else {
                    logger.logAndThrowError(
                        errorMessage = "Type for not provided argument `${missingArgument.name}` couldn't get resolved.",
                        targetClass = targetClass
                    )
                }
            }
        }

        missingArgumentDeclarationText += "\t${missingArgument.name?.asString()}: ${missingArgumentType.getName()}"

        if (argumentTypes.isNotEmpty()) {
            missingArgumentDeclarationText += DIAMOND_OPERATOR_OPEN
            argumentTypes.forEachIndexed { argumentTypeIndex: Int, argumentType: ArgumentType ->
                val typeSeparator: String = getArgumentDeclarationLineEnding(hasNextLine = argumentTypes.lastIndex != argumentTypeIndex, addSpace = true)
                when (argumentType) {
                    is ArgumentType.ArgumentClass -> {
                        val argumentClass: KSType = argumentType.ksType

                        missingArgumentDeclarationText += convertTypeArgumentToString(argumentClass.getName(), ArrayDeque(argumentClass.arguments))
                        missingArgumentDeclarationText += argumentClass.markedNullableAsString() + typeSeparator
                        packageImports.addImport(argumentClass)
                    }

                    ArgumentType.Asterix -> missingArgumentDeclarationText += "*$typeSeparator"
                }
            }
            missingArgumentDeclarationText += DIAMOND_OPERATOR_CLOSE
        }

        missingArgumentDeclarationText += missingArgumentType.markedNullableAsString()

        val lineEnding: String = getArgumentDeclarationLineEnding(hasNextLine = !isLastIndex)
        missingArgumentDeclarationText += "$lineEnding\n"

        return missingArgumentDeclarationText
    }

    private fun extractMatchingAndMissingConstructorArguments(
        targetClass: KSClassDeclaration,
        originClass: KSType,
        targetClassTypeParameters: List<KSTypeParameter>,
        packageImports: PackageImports,
        originClassName: String,
        targetClassName: String
    ): Pair<MutableList<KSValueParameter>, MutableList<MatchingArgument>> {

        val missingArguments = mutableListOf<KSValueParameter>()
        val matchingArguments = mutableListOf<MatchingArgument>()

        targetClass.primaryConstructor?.parameters?.forEach { valueParam: KSValueParameter ->

            val valueName: String = valueParam.name?.asString()!!
            var matchingArgument: MatchingArgument? = null

            // Search for any matching fields, also considers fields of supertype
            (originClass.declaration as KSClassDeclaration).getAllProperties().forEach { parameterFromOriginClass: KSPropertyDeclaration ->
                val parameterNameFromOriginClass: String = parameterFromOriginClass.simpleName.asString()
                val aliases: ArrayList<String>? = parameterFromOriginClass.annotations
                    .firstOrNull { ksAnnotation -> ksAnnotation.shortName.asString() == KCONMAPPER_PROPERTY_ANNOTATION_NAME }
                    ?.arguments
                    ?.firstOrNull()
                    ?.value as? ArrayList<String>

                // The argument matches if either the actual name or the alias from the KConMapperProperty annotation is the same
                if ((parameterNameFromOriginClass == valueName) || (aliases?.any { alias -> alias == valueName } == true)) {
                    val parameterTypeFromTargetClass: KSType = valueParam.type.resolve()
                    val parameterTypeFromOriginClass: KSType = parameterFromOriginClass.type.resolve()
                    val referencedTargetClassGenericTypeParameter: KSTypeParameter? = targetClassTypeParameters.firstOrNull { targetClassTypeParam ->
                        targetClassTypeParam.simpleName.asString() == parameterTypeFromTargetClass.getName()
                    }

                    val targetClassTypeParamUpperBoundDeclaration: KSDeclaration? = referencedTargetClassGenericTypeParameter
                        ?.bounds
                        ?.firstOrNull()
                        ?.resolve()
                        ?.declaration

                    if (targetClassTypeParamUpperBoundDeclaration != null &&
                        parameterTypeFromOriginClass.declaration.containsSupertype(targetClassTypeParamUpperBoundDeclaration) &&
                        evaluateKSTypeAssignable(
                            parameterTypeFromOriginClass = parameterTypeFromOriginClass,
                            parameterTypeFromTargetClass = parameterTypeFromTargetClass,
                            isGenericType = true
                        )
                    ) {
                        matchingArgument = MatchingArgument(
                            targetClassPropertyName = valueName,
                            originClassPropertyName = parameterNameFromOriginClass,
                            targetClassPropertyGenericTypeName = run {
                                if (targetClassTypeParamUpperBoundDeclaration.containingFile != null) {
                                    packageImports.addImport(targetClassTypeParamUpperBoundDeclaration.packageName.asString(), targetClassTypeParamUpperBoundDeclaration.getName())
                                }
                                targetClassTypeParamUpperBoundDeclaration.getName() + parameterTypeFromTargetClass.markedNullableAsString()
                            }
                        )
                    } else if (evaluateKSTypeAssignable(
                            parameterTypeFromOriginClass = parameterTypeFromOriginClass,
                            parameterTypeFromTargetClass = parameterTypeFromTargetClass,
                            isGenericType = referencedTargetClassGenericTypeParameter != null
                        )
                    ) {
                        matchingArgument = MatchingArgument(
                            targetClassPropertyName = valueName,
                            originClassPropertyName = parameterNameFromOriginClass,
                            targetClassPropertyGenericTypeName = referencedTargetClassGenericTypeParameter?.let { typeParam: KSTypeParameter ->
                                typeParam.simpleName.asString() + parameterTypeFromTargetClass.markedNullableAsString()
                            }
                        )
                    } else {
                        logger.warn(
                            message = "Found matching parameter from class `$originClassName` for property `$valueName` of " +
                                    "targetClass `$targetClassName` but the type `${parameterTypeFromOriginClass}` " +
                                    "doesn't match target type `${parameterTypeFromTargetClass}`.",
                            symbol = targetClass
                        )
                    }
                }
            }

            if (matchingArgument != null) {
                matchingArguments.add(matchingArgument!!)
            } else if (!valueParam.hasDefault) {
                // If no matching field could be found nor a default value got provided,
                // we will later add the field as required input-parameter to the function head.
                missingArguments.add(valueParam)
            }
        }

        return Pair(missingArguments, matchingArguments)
    }

    /**
     * Checks if any of respective [KSDeclaration] contains the [searchedSuperType].
     *
     * @param searchedSuperType super type to check for in the [KSDeclaration]
     */
    private fun KSDeclaration.containsSupertype(searchedSuperType: KSDeclaration): Boolean {
        val classDeclaration: KSClassDeclaration = this.qualifiedName?.let(resolver::getClassDeclarationByName) ?: return false
        val containsSuperType: Boolean = classDeclaration.superTypes.any { superType: KSTypeReference ->
            val comparableSuperTypeDeclaration: KSDeclaration = superType.resolve().declaration
            searchedSuperType.compareByQualifiedName(comparableSuperTypeDeclaration) || comparableSuperTypeDeclaration.containsSupertype(searchedSuperType)
        }

        return containsSuperType
    }

    private fun convertTypeArgumentToString(
        typeText: String,
        typeParametersDequeue: ArrayDeque<KSTypeArgument>,
        shouldAddOpenOperator: Boolean = true
    ): String {

        val typeParameter: KSTypeArgument = typeParametersDequeue.removeFirstOrNull() ?: return typeText
        val resolvedTypeParameter: KSType = typeParameter.type?.resolve() ?: return typeText
        var appendedTypeText: String = typeText

        if (shouldAddOpenOperator) appendedTypeText += DIAMOND_OPERATOR_OPEN

        // Add current parameter to the text and also add recursively all declared types
        appendedTypeText += resolvedTypeParameter.getName()
        appendedTypeText += convertTypeArgumentToString("", ArrayDeque(resolvedTypeParameter.arguments))

        val typeParamLineEnding: String = getArgumentDeclarationLineEnding(hasNextLine = typeParametersDequeue.isNotEmpty(), addSpace = true)

        return if (typeParametersDequeue.isNotEmpty()) {

            appendedTypeText += typeParamLineEnding

            // We don't add the open operator because we just string arguments together at this point
            convertTypeArgumentToString(
                typeText = appendedTypeText,
                typeParametersDequeue = typeParametersDequeue,
                shouldAddOpenOperator = false
            )
        } else {
            // No more arguments for this level, we can close the type argument
            appendedTypeText += DIAMOND_OPERATOR_CLOSE
            appendedTypeText += typeParamLineEnding
            appendedTypeText
        }
    }

    /**
     * @param parameterTypeFromOriginClass
     * @param parameterTypeFromTargetClass
     * @param isGenericType accepts that the argument names differ
     */
    private fun evaluateKSTypeAssignable(
        parameterTypeFromOriginClass: KSType,
        parameterTypeFromTargetClass: KSType,
        isGenericType: Boolean
    ): Boolean {
        if (parameterTypeFromOriginClass.isMarkedNullable && !parameterTypeFromTargetClass.isMarkedNullable) return false
        if (!isGenericType && !parameterTypeFromOriginClass.compareByDeclaration(parameterTypeFromTargetClass.declaration)) return false
        return isGenericType || parameterTypeFromOriginClass.arguments.matches(parameterTypeFromTargetClass.arguments)
    }

    private fun List<KSTypeArgument>.matches(otherArguments: List<KSTypeArgument>): Boolean {
        if (isEmpty() == otherArguments.isEmpty()) return true
        if (size != otherArguments.size) return false
        return this.all { argFromThis: KSTypeArgument ->
            otherArguments.firstOrNull { argFromOther: KSTypeArgument ->
                val argTypeFromThis: KSType = argFromThis.type?.resolve() ?: return@all false
                val argTypeFromOther: KSType = argFromOther.type?.resolve() ?: return@all false
                val hasSameTypeName: Boolean = argTypeFromThis.compareByDeclaration(argTypeFromOther.declaration)
                val matchesNullability: Boolean = if (argTypeFromThis.isMarkedNullable) !argTypeFromOther.isMarkedNullable else true

                return@firstOrNull hasSameTypeName &&
                        matchesNullability &&
                        (argFromThis.type?.resolve()?.arguments?.matches(argTypeFromOther.arguments) == true)
            } != null
        }
    }

    private fun generateExtensionFunctionName(originClassName: String, targetClassName: String) = "$originClassName.to$targetClassName"

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

    private fun getArgumentDeclarationLineEnding(hasNextLine: Boolean, addSpace: Boolean = false): String = if (hasNextLine) "," + if (addSpace) " " else "" else ""

    @Suppress("UNCHECKED_CAST")
    private fun extractOriginClasses(kcmAnnotation: KSAnnotation): List<KSType> {
        return kcmAnnotation
            .arguments
            .find { annotationArgument: KSValueArgument -> annotationArgument.name?.asString() == KCONMAPPER_FROM_CLASSES_ANNOTATION_ARG_NAME }!!
            .value as List<KSType>
    }
}