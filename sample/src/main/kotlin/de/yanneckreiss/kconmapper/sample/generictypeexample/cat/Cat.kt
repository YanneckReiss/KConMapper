package de.yanneckreiss.kconmapper.sample.generictypeexample.cat

import de.yanneckreiss.kconmapper.sample.generictypeexample.Animal

abstract class Cat(
    override val name: String,
    override val color: String,
    val meowsOften: Boolean
) : Animal
