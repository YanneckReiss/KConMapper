package de.yanneckreiss.kconmapper.sample_multi_module

import com.github.yanneckreiss.kconmapper.annotations.KConMapper
import com.github.yanneckreiss.kconmapper.annotations.KConMapperProperty
import de.yanneckreiss.kconmapper.sample.entitysample.db.UserEntity
import java.util.*

/**
 * Tries to map to a class from another module.
 *
 * KSP can't resolve annotations from other modules.
 * Therefore, the [KConMapperProperty] form [UserEntity] has no effect here.
 *
 * Only the [KConMapperProperty] from the [User] class will be considered.
 *
 */
@KConMapper(
    fromClasses = [UserEntity::class],
    toClasses = [UserEntity::class],
)
data class User(

    val uid: UUID,

    @KConMapperProperty(aliases = ["name"])
    val fullName: String,
)
