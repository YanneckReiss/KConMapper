package de.yanneckreiss.kconsample.generictypeexample.cat

import de.yanneckreiss.kconsample.generictypeexample.Animal

abstract class Cat(
    override val name: String,
    override val color: String,
    val meowsOften: Boolean
) : Animal
