package de.yanneckreiss.kconmapper.annotations

import kotlin.reflect.KClass

/**
 * Can be used to automatically generate mapper extension functions
 */
annotation class KConMapper(val classes: Array<KClass<*>>)
