package de.yanneckreiss.kconmapper.sample.generictypeexample.dog

import de.yanneckreiss.kconmapper.sample.generictypeexample.Animal

abstract class Dog(
    override val name: String,
    override val color: String,
    val barksOften: Boolean
) : Animal
