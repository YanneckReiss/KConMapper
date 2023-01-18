package de.yanneckreiss.kconmapper.sample.entitysample.dto

import de.yanneckreiss.kconmapper.sample.entitysample.model.Address
import java.util.*

data class UpdateUserDTO(
    val uid: UUID,
    val name: String,
    val address: Address,
)