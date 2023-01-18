package de.yanneckreiss.kconmapper.processor.visitor.argument

import com.google.devtools.ksp.symbol.KSType

sealed class ArgumentType {
    class ArgumentClass(val ksType: KSType) : ArgumentType()
    object Asterix : ArgumentType()
}