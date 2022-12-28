package de.yanneckreiss.kconsample.entitysample.dto

import de.yanneckreiss.kconsample.entitysample.model.Address

data class CreateUserDTO(
    val name: String,
    val address: Address
)