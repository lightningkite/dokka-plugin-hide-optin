import java.net.URI
import com.lightningkite.deployhelpers.*

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.dokka") version "2.0.0" // Used to create a javadoc jar
    `maven-publish`
    signing
}

buildscript {
    repositories {
        mavenLocal()
        maven("https://lightningkite-maven.s3.us-west-2.amazonaws.com")
    }
    dependencies {
        classpath("com.lightningkite:lk-gradle-helpers:3.0.7")
    }
}

group = "com.lightningkite"
version = "main-SNAPSHOT"
useGitBasedVersion()
useLocalDependencies()
publishing()
setupDokka("lightningkite", "dokka-plugin-hide-optin")

repositories {
    mavenCentral()
}

val dokkaVersion: String by project
dependencies {
    compileOnly("org.jetbrains.dokka:dokka-core:$dokkaVersion")
    implementation("org.jetbrains.dokka:dokka-base:$dokkaVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.dokka:dokka-test-api:$dokkaVersion")
    testImplementation("org.jetbrains.dokka:dokka-base-test-utils:$dokkaVersion")
    testImplementation("org.jetbrains.dokka:analysis-kotlin-symbols:$dokkaVersion")
}

kotlin {
    jvmToolchain(8)
}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

java {
    withSourcesJar()
}


publishing {
    repositories {
        val lightningKiteMavenAwsAccessKey: String? = project.findProperty("lightningKiteMavenAwsAccessKey") as? String
        val lightningKiteMavenAwsSecretAccessKey: String? = project.findProperty("lightningKiteMavenAwsSecretAccessKey") as? String
        lightningKiteMavenAwsAccessKey?.let { ak ->
            maven {
                name = "LightningKite"
                url = URI.create("s3://lightningkite-maven")
                credentials(AwsCredentials::class) {
                    accessKey = ak
                    secretKey = lightningKiteMavenAwsSecretAccessKey!!
                }
            }
        }
    }
}

