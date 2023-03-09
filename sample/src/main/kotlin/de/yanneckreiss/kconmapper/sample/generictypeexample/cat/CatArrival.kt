package de.yanneckreiss.kconmapper.sample.generictypeexample.cat

import de.yanneckreiss.kconmapper.generated.toAnimalCareStation
import de.yanneckreiss.kconmapper.generated.toCatCareStation
import de.yanneckreiss.kconmapper.generated.toDogCareStation
import de.yanneckreiss.kconmapper.sample.generictypeexample.AnimalCareStation
import de.yanneckreiss.kconmapper.sample.generictypeexample.cat.BritishShorthair
import de.yanneckreiss.kconmapper.sample.generictypeexample.cat.CatCareStation
import de.yanneckreiss.kconmapper.sample.generictypeexample.dog.DogCareStation
import de.yanneckreiss.kconmapper.sample.generictypeexample.dog.ShibaInu
import de.yanneckreiss.kconmapper.sample_multi_module.generictypeexample.Animal
import java.time.LocalDateTime

class CatArrival {

    fun main(args: Array<String>) {
        val cat1 = BritishShorthair("Caty", "Grey")
        val cat2 = BritishShorthair("Kitty", "Brown")
        val dog1 = ShibaInu("Akiko", "Orange")
        val dog2 = ShibaInu("Aimi", "Orange")

        val now: LocalDateTime = LocalDateTime.now()

        val animalCareStationFromCats: AnimalCareStation<Animal> = CatCareStation(cat1, cat2, now).toAnimalCareStation<Animal>() // Generated extension
        val animalCareStationFromDogs: AnimalCareStation<Animal> = DogCareStation(dog1, dog2, now).toAnimalCareStation<Animal>() // Generated extension

        // TODO: Both of these should not need input parameters here
        val catCareStation: CatCareStation = animalCareStationFromCats.toCatCareStation(cat1, cat2)
        val dogStation: DogCareStation = animalCareStationFromCats.toDogCareStation(dog1, dog2)

        println(animalCareStationFromCats.toString())
        println(animalCareStationFromDogs.toString())
    }
}