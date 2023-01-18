package de.yanneckreiss.kconmapper.sample.generictypeexample.cat

import com.github.yanneckreiss.kconmapper.annotations.KConMapper
import com.github.yanneckreiss.kconmapper.annotations.KConMapperProperty
import de.yanneckreiss.kconmapper.sample.generictypeexample.AnimalCareStation
import java.time.LocalDateTime

@KConMapper(fromClasses = [AnimalCareStation::class])
class CatCareStation(

    @KConMapperProperty(alternativePropertyName = "animalOne")
    val catOne: Cat,

    @KConMapperProperty(alternativePropertyName = "animalTwo")
    val catTwo: Cat,

    val timestamp: LocalDateTime
)