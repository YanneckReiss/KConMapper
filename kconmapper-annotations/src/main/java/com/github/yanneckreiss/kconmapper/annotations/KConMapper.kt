package com.github.yanneckreiss.kconmapper.annotations

import kotlin.reflect.KClass

/**
 *
 * @param toClasses define classes you want to map to from the annotated class.
 *        The result will be `KConMapperAnnotatedClass.toClassFromAnnotationParameter(): ClassFromAnnotationParameter`
 * @param fromClasses define classes you want to map from to the annotated class.
 *        The result will be `ClassFromAnnotationParameter.toKConMapperAnnotatedClass(): KConMapperAnnotatedClass`
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KConMapper(
    val toClasses: Array<KClass<*>> = [],
    val fromClasses: Array<KClass<*>> = []
)
