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
    `maven-publish`
}

repositories {
    google()
    mavenCentral()
}

subprojects {

    apply(plugin = "java")

    if (!project.name.contains("sample", ignoreCase = true)) {
        apply(plugin = "maven-publish")

        publishing {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/yanneckreiss/KConMapper")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }

            publications {
                register<MavenPublication>("gpr") {
                    groupId = "com.github.yanneckreiss"
                    artifactId = "kconmapper"
                    version = "1.0.0-alpha02"

                    from(components["java"])

                    pom {
                        name.set("KConMapper")
                        description.set("A KSP plugin for generating constructor mapping extension functions.")
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("yanneckreiss")
                                name.set("Yanneck Reiß")
                                email.set("support@kaspic.de")
                            }
                        }
                    }
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
