plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    idea
}

sourceSets.configureEach {
    kotlin.srcDir("$buildDir/generated/ksp/$name/kotlin/")
}

dependencies {
    implementation(project(":sample"))
    implementation(project(":kconmapper-annotations"))
    ksp(project(":kconmapper-ksp"))
}
