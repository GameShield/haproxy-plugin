plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
}

group = "io.gameshield"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.abelix.club/repository/public/")
}

dependencies {
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    implementation("io.netty:netty-codec-haproxy:4.1.0.CR3")

    compileOnly("org.spigotmc:plugin-annotations:+")
    annotationProcessor("org.spigotmc:plugin-annotations:+")

    compileOnly("org.jetbrains:annotations:+")

    compileOnly("org.projectlombok:lombok:+")
    annotationProcessor("org.projectlombok:lombok:+")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GITHUB"
            url = uri("https://maven.pkg.github.com/gameshield/haproxy-plugin")
            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
    }
}

tasks.build.get().dependsOn(tasks.shadowJar)