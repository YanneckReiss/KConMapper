package de.yanneckreiss.kconsample.generictypeexample

import de.yanneckreiss.kconmapper.annotations.KConMapper
import de.yanneckreiss.kconsample.generictypeexample.cat.CatCareStation
import de.yanneckreiss.kconsample.generictypeexample.dog.DogCareStation
import java.time.LocalDateTime

@KConMapper(classes = [CatCareStation::class, DogCareStation::class])
class AnimalCareStation<C : Animal>(
    val animalOne: C,
    val animalTwo: C,
    val timestamp: LocalDateTime
)