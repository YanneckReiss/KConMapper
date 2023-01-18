package de.yanneckreiss.kconmapper.sample.generictypeexample.dog

class ShibaInu(
    override val name: String,
    override val color: String,
) : Dog(
    name = name,
    color = color,
    barksOften = false
) {
    override fun pet() {
        // Pet me
    }
}
