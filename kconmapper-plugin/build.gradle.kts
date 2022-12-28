plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "de.yanneckreiss"
version = "1.0.0-alpha01"

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/yanneckreiss/KConMapper")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("PUBLISH_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
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