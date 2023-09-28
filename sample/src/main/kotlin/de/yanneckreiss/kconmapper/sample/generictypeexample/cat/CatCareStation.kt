package de.yanneckreiss.kconmapper.sample.generictypeexample.cat

import com.github.yanneckreiss.kconmapper.annotations.KConMapperProperty
import java.time.LocalDateTime

class CatCareStation(

    @KConMapperProperty(aliases = ["animalOne"])
    val catOne: Cat,

    @KConMapperProperty(aliases = ["animalTwo"])
    val catTwo: Cat,

    val timestamp: LocalDateTime
)