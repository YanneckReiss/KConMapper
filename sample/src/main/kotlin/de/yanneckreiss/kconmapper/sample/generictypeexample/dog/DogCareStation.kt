package de.yanneckreiss.kconmapper.sample.generictypeexample.dog

import com.github.yanneckreiss.kconmapper.annotations.KConMapperProperty
import java.time.LocalDateTime

class DogCareStation(

    @KConMapperProperty(aliases = ["animalOne"])
    val dogOne: Dog,

    @KConMapperProperty(aliases = ["animalTwo"])
    val dogTwo: Dog,

    val timestamp: LocalDateTime
)