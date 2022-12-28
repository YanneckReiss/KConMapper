package de.yanneckreiss.kconsample.entitysample.db

import de.yanneckreiss.kconmapper.annotations.KConMapper
import de.yanneckreiss.kconsample.entitysample.dto.CreateUserDTO
import de.yanneckreiss.kconsample.entitysample.dto.UpdateUserDTO
import de.yanneckreiss.kconsample.entitysample.model.Address
import java.util.*

@KConMapper(
    [
        CreateUserDTO::class,
        UpdateUserDTO::class
    ]
)
data class UserEntity(
    val uid: UUID = UUID.randomUUID(),
    val name: String,
    val address: Address,
)
