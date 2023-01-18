package com.github.yanneckreiss.kconmapper.annotations

import kotlin.reflect.KClass

/**
 * Can be used to automatically generate mapper extension functions
 *
 * @param targetClasses define classes you want to map to. The result will be `OriginClass.toTargetClass(): TargetClass`
 * @param fromClasses define classes you want to map from. The result will be `TargetClass.toOriginClass(): OriginClass`
 */
annotation class KConMapper(
    val targetClasses: Array<KClass<*>> = [],
    val fromClasses: Array<KClass<*>> = []
)
