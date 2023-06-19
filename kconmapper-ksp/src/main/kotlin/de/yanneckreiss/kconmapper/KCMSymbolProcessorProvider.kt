package de.yanneckreiss.kconmapper

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import de.yanneckreiss.kconmapper.processor.KCMSymbolProcessor
import de.yanneckreiss.kconmapper.processor.common.KConMapperConfiguration

class KCMSymbolProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KCMSymbolProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            kConMapperConfiguration = KConMapperConfiguration(environment.options)
        )
    }
}
