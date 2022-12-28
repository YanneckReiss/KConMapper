package de.yanneckreiss.kconsample.generictypeexample.dog

import de.yanneckreiss.kconsample.generictypeexample.Animal

abstract class Dog(
    override val name: String,
    override val color: String,
    val barksOften: Boolean
) : Animal
