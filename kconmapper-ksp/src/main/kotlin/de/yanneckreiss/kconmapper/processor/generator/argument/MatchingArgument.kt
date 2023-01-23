package de.yanneckreiss.kconmapper.processor.generator.argument

data class MatchingArgument(
    val targetClassPropertyName: String,
    val sourceClassPropertyName: String,

    // Only defined if the target class parameter is generic
    val targetClassPropertyGenericTypeName: String? = null
)