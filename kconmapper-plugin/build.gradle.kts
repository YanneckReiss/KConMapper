plugins {
    kotlin("jvm")
    `maven-publish`
}

publishing {
    repositories {
        maven("https://maven.pkg.github.com/yanneckreiss/kconmapper") {
            name = "KConMapper"
            version = "1.0.0-alpha01"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("PUBLISH_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("gpr") {
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