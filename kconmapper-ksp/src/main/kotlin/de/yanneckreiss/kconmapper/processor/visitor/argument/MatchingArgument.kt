package de.yanneckreiss.kconmapper.processor.visitor.argument

data class MatchingArgument(
    val targetClassPropertyName: String,
    val originClassPropertyName: String,

    // Only defined if the target class parameter is generic
    val targetClassPropertyGenericTypeName: String? = null
)