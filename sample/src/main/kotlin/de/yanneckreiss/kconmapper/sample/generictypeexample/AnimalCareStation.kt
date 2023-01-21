package de.yanneckreiss.kconmapper.sample.generictypeexample

import com.github.yanneckreiss.kconmapper.annotations.KConMapper
import de.yanneckreiss.kconmapper.sample.generictypeexample.cat.CatCareStation
import de.yanneckreiss.kconmapper.sample.generictypeexample.dog.DogCareStation
import java.time.LocalDateTime

@KConMapper(fromClasses = [CatCareStation::class, DogCareStation::class])
class AnimalCareStation<C : Animal>(
    val animalOne: C,
    val animalTwo: C,
    val timestamp: LocalDateTime
)