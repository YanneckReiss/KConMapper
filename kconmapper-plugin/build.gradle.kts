plugins {
    kotlin("jvm")
    `maven-publish`
}

publishing {
    repositories {
        maven("https://maven.pkg.github.com/yanneckreiss/KConMapper") {
            name = "GitHubPackages"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("PUBLISH_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("gpr") {
            groupId = "com.github.yanneckreiss"
            artifactId = "kconmapper"
            version = "1.0.0-alpha01"

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

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.21-1.0.8")
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}