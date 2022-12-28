plugins {
    kotlin("jvm")
    `maven-publish`
}

publishing {
    repositories {
        maven("https://maven.pkg.github.com/yanneckreiss/KConMapper") {

            name = "KConMapper"

            credentials(HttpHeaderCredentials::class.java) {
                name = System.getenv("Authorization")
                value = "Bearer ${System.getenv("PUBLISH_TOKEN")}"
            }

            authentication {
                create<HttpHeaderAuthentication>("header")
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