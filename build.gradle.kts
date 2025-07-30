import java.net.URI
import com.lightningkite.deployhelpers.*

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.dokka") version "2.0.0" // Used to create a javadoc jar
    id("com.vanniktech.maven.publish") version "0.34.0" // Used to create a javadoc jar
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


mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    pom {
        name.set("KotlinX Serialization CSV Durable")
        description.set("A format for KotlinX Serialization that handles CSV files with a header row.")
        github("lightningkite", "kotlinx-serialization-csv-durable")
        url.set(dokkaPublicHostingIndex)
        licenses { mit() }
        developers {
            joseph()
            brady()
        }
    }
}

