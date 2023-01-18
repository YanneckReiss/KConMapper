package com.github.yanneckreiss.kconmapper.annotations

/**
 * Can be used if you want to assign a property from the origin class to a property
 * of the target class that has a different name than the property of the origin class.
 */
annotation class KConMapperProperty(val alternativePropertyName: String)
