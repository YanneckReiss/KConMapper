package com.github.yanneckreiss.kconmapper.annotations

/**
 * Can be assigned to a property of the sourceClass
 * if you want to provide an alias for the variable name.
 *
 * Useful if the target class has a different name than the
 * property of the sourceClass class but describes the same property.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class KConMapperProperty(val aliases: Array<String>)
