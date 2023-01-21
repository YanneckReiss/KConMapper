package de.yanneckreiss.kconmapper.sample.entitysample.db

import com.github.yanneckreiss.kconmapper.annotations.KConMapper
import de.yanneckreiss.kconmapper.sample.entitysample.dto.CreateUserDTO
import de.yanneckreiss.kconmapper.sample.entitysample.dto.UpdateUserDTO
import de.yanneckreiss.kconmapper.sample.entitysample.model.Address
import java.util.*

@KConMapper(fromClasses = [CreateUserDTO::class, UpdateUserDTO::class], targetClasses = [CreateUserDTO::class, UpdateUserDTO::class])
data class UserEntity(
    val uid: UUID = UUID.randomUUID(),
    val name: String,
    val address: Address,
)
