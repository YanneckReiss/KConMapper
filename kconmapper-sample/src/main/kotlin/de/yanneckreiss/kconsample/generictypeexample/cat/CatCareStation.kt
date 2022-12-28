package de.yanneckreiss.kconsample.generictypeexample.cat

import de.yanneckreiss.kconmapper.annotations.KConMapper
import de.yanneckreiss.kconmapper.annotations.KConMapperProperty
import de.yanneckreiss.kconsample.generictypeexample.AnimalCareStation
import java.time.LocalDateTime

@KConMapper(classes = [AnimalCareStation::class])
class CatCareStation(

    @KConMapperProperty(targetClassPropertyName = "animalOne")
    val catOne: Cat,

    @KConMapperProperty(targetClassPropertyName = "animalTwo")
    val catTwo: Cat,

    val timestamp: LocalDateTime
)