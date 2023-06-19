package de.yanneckreiss.kconmapper.processor.common

private const val OPTION_SUPPRESS_MAPPING_MISMATCH_WARNINGS = "kconmapper.suppressMappingMismatchWarnings"

data class KConMapperConfiguration(
    val suppressMappingMismatchWarnings: Boolean
) {
    constructor(options: Map<String, String>) : this(
        suppressMappingMismatchWarnings = options[OPTION_SUPPRESS_MAPPING_MISMATCH_WARNINGS]?.toBoolean() ?: false
    )
}
