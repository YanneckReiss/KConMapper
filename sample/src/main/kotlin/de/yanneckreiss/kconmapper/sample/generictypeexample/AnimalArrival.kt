package de.yanneckreiss.kconmapper.sample.generictypeexample

import de.yanneckreiss.kconmapper.generated.toAnimalCareStation
import de.yanneckreiss.kconmapper.sample.generictypeexample.cat.BritishShorthair
import de.yanneckreiss.kconmapper.sample.generictypeexample.cat.Cat
import de.yanneckreiss.kconmapper.sample.generictypeexample.cat.CatCareStation
import de.yanneckreiss.kconmapper.sample.generictypeexample.dog.Dog
import de.yanneckreiss.kconmapper.sample.generictypeexample.dog.DogCareStation
import de.yanneckreiss.kconmapper.sample.generictypeexample.dog.ShibaInu
import java.time.LocalDateTime

class AnimalArrival {

    fun main(args: Array<String>) {


        val cat1 = BritishShorthair("Caty", "Grey")
        val cat2 = BritishShorthair("Kitty", "Brown")
        val dog1 = ShibaInu("Akiko", "Orange")
        val dog2 = ShibaInu("Aimi", "Orange")

        val now: LocalDateTime = LocalDateTime.now()

        val animalCareStationFromCats: AnimalCareStation<Animal> = CatCareStation(cat1, cat2, now).toAnimalCareStation<Cat>() // Generated extension
        val animalCareStationFromDogs: AnimalCareStation<Animal> = DogCareStation(dog1, dog2, now).toAnimalCareStation<Dog>() // Generated extension

        println(animalCareStationFromCats.toString())
        println(animalCareStationFromDogs.toString())
    }
}