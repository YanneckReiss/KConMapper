package de.yanneckreiss.kconmapper.sample.generictypeexample.cat

import com.github.yanneckreiss.kconmapper.annotations.KConMapper
import com.github.yanneckreiss.kconmapper.annotations.KConMapperProperty
import de.yanneckreiss.kconmapper.sample.generictypeexample.AnimalCareStation
import java.time.LocalDateTime

@KConMapper(toClasses = [AnimalCareStation::class])
class CatCareStation(

    @KConMapperProperty(aliases = ["animalOne"])
    val catOne: Cat,

    @KConMapperProperty(aliases = ["animalTwo"])
    val catTwo: Cat,

    val timestamp: LocalDateTime
)