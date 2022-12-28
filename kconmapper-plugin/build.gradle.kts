plugins {
    kotlin("jvm")
    `maven-publish`
}

publishing {
    repositories {
        maven {
            name = "KConMapper"
            url = uri("https://maven.pkg.github.com/yanneckreiss/KConMapper")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
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