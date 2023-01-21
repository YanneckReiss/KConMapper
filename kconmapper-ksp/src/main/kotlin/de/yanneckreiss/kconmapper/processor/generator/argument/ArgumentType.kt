package de.yanneckreiss.kconmapper.processor.generator.argument

import com.google.devtools.ksp.symbol.KSType

sealed class ArgumentType {
    class ArgumentClass(val ksType: KSType) : ArgumentType()
    object Asterix : ArgumentType()
}