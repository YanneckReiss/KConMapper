package de.yanneckreiss.kconsample.entitysample.dto

import de.yanneckreiss.kconsample.entitysample.model.Address
import java.util.*

data class UpdateUserDTO(
    val uid: UUID,
    val name: String,
    val address: Address,
)