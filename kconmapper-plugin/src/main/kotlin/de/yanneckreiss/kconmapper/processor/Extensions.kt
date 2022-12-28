package de.yanneckreiss.kconmapper.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType

private const val NULLABLE_MARK = "?"

fun KSType.getName() = declaration.getName()
fun KSDeclaration.getName() = simpleName.asString()

@Throws(IllegalArgumentException::class)
fun KSPLogger.logAndThrowError(errorMessage: String, targetClass: KSClassDeclaration) {
    error(errorMessage, targetClass as KSNode)
    throw IllegalArgumentException(errorMessage)
}

fun KSType.compareByDeclaration(otherDeclaration: KSDeclaration): Boolean = this.declaration.compareByQualifiedName(otherDeclaration)

fun KSDeclaration.compareByQualifiedName(other: KSDeclaration): Boolean {
    val thisName: String = this.packageName.asString() + "." + this.simpleName.asString()
    val otherName: String = other.packageName.asString() + "." + other.simpleName.asString()
    return thisName == otherName
}

fun KSType.markedNullableAsString() = if (isMarkedNullable) NULLABLE_MARK else ""
