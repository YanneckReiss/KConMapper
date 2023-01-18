package de.yanneckreiss.kconmapper.sample.entitysample.dto

import de.yanneckreiss.kconmapper.sample.entitysample.model.Address

data class CreateUserDTO(
    val name: String,
    val address: Address
)