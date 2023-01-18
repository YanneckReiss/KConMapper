package de.yanneckreiss.kconmapper.sample.generictypeexample.dog

import com.github.yanneckreiss.kconmapper.annotations.KConMapper
import com.github.yanneckreiss.kconmapper.annotations.KConMapperProperty
import de.yanneckreiss.kconmapper.sample.generictypeexample.AnimalCareStation
import java.time.LocalDateTime

@KConMapper(fromClasses = [AnimalCareStation::class])
class DogCareStation(

    @KConMapperProperty(propertyNames = ["animalOne"])
    val dogOne: Dog,

    @KConMapperProperty(propertyNames = ["animalTwo"])
    val dogTwo: Dog,

    val timestamp: LocalDateTime
)