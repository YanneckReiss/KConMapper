package de.yanneckreiss.kconmapper.base

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import de.yanneckreiss.kconmapper.KCMSymbolProcessorProvider
import de.yanneckreiss.kconmapper.processor.KCMConstants
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class KSPCompilerTest {

    @TempDir
    protected lateinit var tempWorkDir: File

    protected fun compileSource(vararg source: SourceFile): KotlinCompilation.Result = KotlinCompilation().apply {
        sources = source.toList()
        symbolProcessorProviders = listOf(KCMSymbolProcessorProvider())
        inheritClassPath = true
        verbose = false
        workingDir = tempWorkDir
        kspWithCompilation = true
    }.compile()

    protected fun KotlinCompilation.Result.loadGeneratedClass(className: String): Class<*> =
        classLoader.loadClass("${KCMConstants.GENERATED_FILE_PATH}.$className")
}
