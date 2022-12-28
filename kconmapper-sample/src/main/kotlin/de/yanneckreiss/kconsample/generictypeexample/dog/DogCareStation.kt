package de.yanneckreiss.kconsample.generictypeexample.dog

import de.yanneckreiss.kconmapper.annotations.KConMapper
import de.yanneckreiss.kconmapper.annotations.KConMapperProperty
import de.yanneckreiss.kconsample.generictypeexample.AnimalCareStation
import java.time.LocalDateTime

@KConMapper(classes = [AnimalCareStation::class])
class DogCareStation(

    @KConMapperProperty(targetClassPropertyName = "animalOne")
    val dogOne: Dog,

    @KConMapperProperty(targetClassPropertyName = "animalTwo")
    val dogTwo: Dog,

    val timestamp: LocalDateTime
)