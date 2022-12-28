package de.yanneckreiss.kconsample.generictypeexample.cat

class BritishShorthair(
    override val name: String,
    override val color: String
) : Cat(
    name = name,
    color = color,
    meowsOften = true
) {
    override fun pet() {
        // Pet me
    }
}
