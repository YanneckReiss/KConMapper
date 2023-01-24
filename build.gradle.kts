import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

allprojects {
    version = "1.0.0-alpha03"
    group = "com.github.yanneckreiss.kconmapper"

    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    kotlin("jvm")
}

subprojects {

    apply(plugin = "java")

    if (!project.name.contains("sample", ignoreCase = true)) {
        apply(plugin = "maven-publish")

        configure<PublishingExtension> {
            publications {
                register<MavenPublication>("maven") {
                    groupId = project.group.toString()
                    artifactId = when {
                        project.name.contains("ksp") -> "ksp"
                        project.name.contains("annotations") -> "annotations"
                        else -> project.name
                    }
                    version = project.version.toString()

                    from(components["java"])
                }
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        withSourcesJar()
        withJavadocJar()
    }

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("stdlib-jdk8"))
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
}
