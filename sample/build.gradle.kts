plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    idea
}

ksp {
    // If set to true, this argument suppresses warnings about mapping mismatches,
    // critical warnings are still emitted.
    arg("kconmapper.suppressMappingMismatchWarnings", "false")
}

kotlin {
    jvmToolchain(17)

    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

dependencies {
    implementation(project(":kconmapper-annotations"))
    ksp(project(":kconmapper-ksp"))
}
