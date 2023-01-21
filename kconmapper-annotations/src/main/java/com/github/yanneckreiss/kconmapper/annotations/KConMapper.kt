package com.github.yanneckreiss.kconmapper.annotations

import kotlin.reflect.KClass

/**
 *
 * @param toClasses define classes you want to map to from the annotated class.
 *        The result will be `SourceClass.toTargetClass(): TargetClass`
 * @param fromClasses define classes you want to map from to the annotated class.
 *        The result will be `TargetClass.toSourceClass(): SourceClass`
 */
annotation class KConMapper(
    val toClasses: Array<KClass<*>> = [],
    val fromClasses: Array<KClass<*>> = []
)
