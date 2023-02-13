package de.yanneckreiss.kconmapper.processor.generator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import de.yanneckreiss.kconmapper.processor.*
import de.yanneckreiss.kconmapper.processor.generator.argument.ArgumentType
import de.yanneckreiss.kconmapper.processor.generator.argument.MatchingArgument
import de.yanneckreiss.kconmapper.processor.visitor.PackageImports

private const val KCONMAPPER_PROPERTY_ANNOTATION_NAME = "KConMapperProperty"
private const val DIAMOND_OPERATOR_OPEN = "<"
private const val DIAMOND_OPERATOR_CLOSE = ">"
private const val KOTLIN_FUNCTION_KEYWORD = "fun"
private const val CLOSE_FUNCTION = ")"
private const val OPEN_FUNCTION = "("

class MappingFunctionGenerator(
    private val resolver: Resolver,
    private val logger: KSPLogger
) {

    /**
     *
     * Generates a function in the form of: `fun SourceClass.toTargetClass(): TargetClass`
     *
     * @param targetClass The class we want to map to.
     *        It defines the schema which the `sourceClass` properties should be mapped to.
     *        Therefore, it is also the return type of the mapper extension function.
     *
     * @param sourceClass The class which is the source for properties we want to map to the constructor of the
     *        target class. It is therefore at the same time the class for which we generate the extension function.
     */
    fun generateMappingFunction(
        sourceClass: KSClassDeclaration,
        targetClass: KSClassDeclaration,
        packageImports: PackageImports
    ): String {

        val targetClassName: String = targetClass.simpleName.getShortName()
        val packageName: String = targetClass.packageName.asString()
        val targetClassTypeParameters: List<KSTypeParameter> = targetClass.typeParameters

        packageImports.targetClassTypeParameters += targetClassTypeParameters

        // Add import for the target class
        packageImports.addImport(packageName, targetClassName)

        // Add import for source class
        packageImports.addImport(sourceClass.packageName.asString(), sourceClass.simpleName.asString())

        // Create mapping extension function for source class to target class
        return generateExtensionMapperFunctionForSourceClass(
            sourceClass = sourceClass,
            targetClass = targetClass,
            targetClassTypeParameters = targetClassTypeParameters,
            targetClassName = targetClassName,
            packageImports = packageImports
        )
    }

    private fun generateExtensionMapperFunctionForSourceClass(
        sourceClass: KSClassDeclaration,
        targetClass: KSClassDeclaration,
        targetClassTypeParameters: List<KSTypeParameter>,
        targetClassName: String,
        packageImports: PackageImports
    ): String {

        var extensionFunctions = ""
        val sourceClassName: String = sourceClass.toString()

        // Find all matching arguments from the source class that can be mapped to the target class
        val (
            missingConstructorArguments: List<KSValueParameter>,
            matchingConstructorArguments: List<MatchingArgument>
        ) = extractMatchingAndMissingConstructorArguments(
            targetClass = targetClass,
            sourceClass = sourceClass,
            targetClassTypeParameters = targetClassTypeParameters,
            packageImports = packageImports,
            sourceClassName = sourceClassName,
            targetClassName = targetClassName
        )

        // Add missing arguments to function head
        if (missingConstructorArguments.isNotEmpty()) {

            // Warn the user if unnecessary KConMapper got declared
            if (matchingConstructorArguments.isEmpty()) {
                logger.warn(
                    message = "\n\nNo matching arguments for mapping from class `$sourceClassName` to `$targetClassName`. \n" +
                            "You can instead remove the argument `$sourceClassName::class` from the `${KCONMAPPER_ANNOTATION_NAME}` annotation in class `$targetClassName` " +
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
                extensionFunctions += "$DIAMOND_OPERATOR_CLOSE ${generateExtensionFunctionName(sourceClass, targetClass, packageImports)}(\n"
            } else {
                extensionFunctions += "$KOTLIN_FUNCTION_KEYWORD ${generateExtensionFunctionName(sourceClass, targetClass, packageImports)}(\n"
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
                extensionFunctions += "$DIAMOND_OPERATOR_CLOSE ${
                    generateExtensionFunctionName(
                        sourceClass = sourceClass,
                        targetClass = targetClass,
                        packageImports = packageImports
                    )
                }$OPEN_FUNCTION$CLOSE_FUNCTION = $targetClassName$OPEN_FUNCTION\n"
            } else {
                extensionFunctions += "$KOTLIN_FUNCTION_KEYWORD ${
                    generateExtensionFunctionName(
                        sourceClass = sourceClass,
                        targetClass = targetClass,
                        packageImports = packageImports
                    )
                }$OPEN_FUNCTION$CLOSE_FUNCTION = $targetClassName$OPEN_FUNCTION\n"
            }
        }

        // Assign matching values from source class to target class constructor
        matchingConstructorArguments.forEachIndexed { index: Int, matchingArgument: MatchingArgument ->
            val lineEnding: String = getArgumentDeclarationLineEnding(hasNextLine = missingConstructorArguments.lastIndex != index || missingConstructorArguments.isNotEmpty())
            extensionFunctions += "\t${matchingArgument.targetClassPropertyName} = this.${matchingArgument.sourceClassPropertyName}"

            // If source class is a generic type parameter we need to cast it to avoid compilation errors
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

        extensionFunctions += "$CLOSE_FUNCTION\n\n"

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
        sourceClass: KSClassDeclaration,
        targetClassTypeParameters: List<KSTypeParameter>,
        packageImports: PackageImports,
        sourceClassName: String,
        targetClassName: String
    ): Pair<MutableList<KSValueParameter>, MutableList<MatchingArgument>> {

        val missingArguments = mutableListOf<KSValueParameter>()
        val matchingArguments = mutableListOf<MatchingArgument>()

        targetClass.primaryConstructor?.parameters?.forEach { valueParam: KSValueParameter ->

            val valueName: String = valueParam.name?.asString()!!
            var matchingArgument: MatchingArgument? = null

            // Search for any matching fields, also considers fields of supertype
            sourceClass.getAllProperties().forEach { parameterFromSourceClass: KSPropertyDeclaration ->
                val parameterNameFromSourceClass: String = parameterFromSourceClass.simpleName.asString()

                // Get the aliases from the KConMapperProperty annotation, can in reality only be of type ArrayList<String>?
                val aliases: ArrayList<*>? = parameterFromSourceClass.annotations
                    .firstOrNull { ksAnnotation -> ksAnnotation.shortName.asString() == KCONMAPPER_PROPERTY_ANNOTATION_NAME }
                    ?.arguments
                    ?.firstOrNull()
                    ?.value as ArrayList<*>?

                // The argument matches if either the actual name or the alias from the KConMapperProperty annotation is the same
                if ((parameterNameFromSourceClass == valueName) || (aliases?.any { alias -> alias == valueName } == true)) {
                    val parameterTypeFromTargetClass: KSType = valueParam.type.resolve()
                    val parameterTypeFromSourceClass: KSType = parameterFromSourceClass.type.resolve()
                    val referencedTargetClassGenericTypeParameter: KSTypeParameter? = targetClassTypeParameters.firstOrNull { targetClassTypeParam ->
                        targetClassTypeParam.simpleName.asString() == parameterTypeFromTargetClass.getName()
                    }

                    val targetClassTypeParamUpperBoundDeclaration: KSDeclaration? = referencedTargetClassGenericTypeParameter
                        ?.bounds
                        ?.firstOrNull()
                        ?.resolve()
                        ?.declaration

                    if (targetClassTypeParamUpperBoundDeclaration != null &&
                        parameterTypeFromSourceClass.declaration.containsSupertype(targetClassTypeParamUpperBoundDeclaration) &&
                        evaluateKSTypeAssignable(
                            parameterTypeFromSourceClass = parameterTypeFromSourceClass,
                            parameterTypeFromTargetClass = parameterTypeFromTargetClass,
                            isGenericType = true
                        )
                    ) {
                        matchingArgument = MatchingArgument(
                            targetClassPropertyName = valueName,
                            sourceClassPropertyName = parameterNameFromSourceClass,
                            targetClassPropertyGenericTypeName = run {
                                if (targetClassTypeParamUpperBoundDeclaration.containingFile != null) {
                                    packageImports.addImport(targetClassTypeParamUpperBoundDeclaration.packageName.asString(), targetClassTypeParamUpperBoundDeclaration.getName())
                                }
                                targetClassTypeParamUpperBoundDeclaration.getName() + parameterTypeFromTargetClass.markedNullableAsString()
                            }
                        )
                    } else if (evaluateKSTypeAssignable(
                            parameterTypeFromSourceClass = parameterTypeFromSourceClass,
                            parameterTypeFromTargetClass = parameterTypeFromTargetClass,
                            isGenericType = referencedTargetClassGenericTypeParameter != null
                        )
                    ) {
                        matchingArgument = MatchingArgument(
                            targetClassPropertyName = valueName,
                            sourceClassPropertyName = parameterNameFromSourceClass,
                            targetClassPropertyGenericTypeName = referencedTargetClassGenericTypeParameter?.let { typeParam: KSTypeParameter ->
                                typeParam.simpleName.asString() + parameterTypeFromTargetClass.markedNullableAsString()
                            }
                        )
                    } else {
                        logger.warn(
                            message = "Found matching parameter from class `$sourceClassName` for property `$valueName` of " +
                                    "targetClass `$targetClassName` but the type `${parameterTypeFromSourceClass}` " +
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
     * @param parameterTypeFromSourceClass
     * @param parameterTypeFromTargetClass
     * @param isGenericType accepts that the argument names differ
     */
    private fun evaluateKSTypeAssignable(
        parameterTypeFromSourceClass: KSType,
        parameterTypeFromTargetClass: KSType,
        isGenericType: Boolean
    ): Boolean {
        if (parameterTypeFromSourceClass.isMarkedNullable && !parameterTypeFromTargetClass.isMarkedNullable) return false
        if (!isGenericType && !parameterTypeFromSourceClass.compareByDeclaration(parameterTypeFromTargetClass.declaration)) return false
        return isGenericType || parameterTypeFromSourceClass.arguments.matches(parameterTypeFromTargetClass.arguments)
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

    private fun generateExtensionFunctionName(
        sourceClass: KSDeclaration,
        targetClass: KSDeclaration,
        packageImports: PackageImports
    ): String {

        val sourceClassName: String = sourceClass.getName()
        val targetClassName: String = targetClass.getName()

        val sourceClassType: String = sourceClass.typeParameters.firstOrNull()?.let KsTypeParameterLet@ { ksTypeParameter: KSTypeParameter ->

            val upperBound = ksTypeParameter.bounds.firstOrNull()?.resolve()?.let UpperBoundLet@{ upperBoundType ->
                packageImports.addImport(upperBoundType)
                return@UpperBoundLet upperBoundType.getName()
            } ?: ""

            DIAMOND_OPERATOR_OPEN + upperBound + DIAMOND_OPERATOR_CLOSE
        } ?: ""

        return "$sourceClassName$sourceClassType.to$targetClassName"
    }

    private fun getArgumentDeclarationLineEnding(hasNextLine: Boolean, addSpace: Boolean = false): String = if (hasNextLine) "," + if (addSpace) " " else "" else ""

}
