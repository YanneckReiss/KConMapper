val kspVersion: String by project

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":kconmapper-annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")

    testApi(kotlin("test"))
    testApi("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.5.0")
    testApi("org.junit.jupiter:junit-jupiter:5.9.2")
    testApi(project(":kconmapper-annotations"))
}

tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview"
}
