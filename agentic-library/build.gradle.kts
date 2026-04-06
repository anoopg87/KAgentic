plugins {
    kotlin("jvm") version "1.9.23"
    id("maven-publish")
}

group = "com.agentic"
version = "0.1.0-alpha"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("net.objecthunter:exp4j:0.4.8")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
    // Test dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.23")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
}

kotlin {
    jvmToolchain(17)
}
tasks.test {
    useJUnitPlatform()
}

// Publishing configuration for GitHub Packages
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "kotlin-agentic"
            version = project.version.toString()
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/anoopg87/KAgentic")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: ""
                password = System.getenv("KAGENTIC_PUBLISH_TOKEN") ?: ""
            }
        }
    }
}
