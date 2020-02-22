import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("jvm") version "1.3.61"
    id("com.diffplug.gradle.spotless") version "3.27.1"
}

repositories {
    mavenCentral()
    jcenter()
}

subprojects {

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")
    apply(plugin = "com.diffplug.gradle.spotless")

    val snapshotRepoUrl = uri("http://localhost:8081/artifactory/libs-snapshot-local")
    val releaseRepoUrl = uri("http://localhost:8081/artifactory/libs-release-local")

    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    artifacts {
        add("archives", sourcesJar)
    }

    publishing {
        repositories {
            maven {
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotRepoUrl else releaseRepoUrl
            }
        }
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])
                artifact(sourcesJar.get())
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenCentral()
        jcenter()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    spotless {
        java {
            target("**/*.java")
            importOrder("java", "javax", "org", "com") // A sequence of package names
            indentWithSpaces(4)
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }

        kotlin {
            target("**/*.kt")
            targetExclude("**/.gradle/**")
            indentWithSpaces(4)
            trimTrailingWhitespace()
            endWithNewline()
            ktlint().userData(mapOf("max_line_length" to "120", "insert_final_newline" to "true", "indentSize" to "4"))
        }

        kotlinGradle {
            // same as kotlin, but for .gradle.kts files (defaults to '*.gradle.kts')
            target("**/*.gradle.kts")

            indentWithSpaces(4)
            trimTrailingWhitespace()
            endWithNewline()
            // Optional user arguments can be set as such:
            ktlint().userData(mapOf("max_line_length" to "120", "insert_final_newline" to "true"))

            // doesn't support licenseHeader, because scripts don't have a package statement
            // to clearly mark where the license should go
        }
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation("org.slf4j:slf4j-api:1.7.25")
        implementation("ch.qos.logback:logback-classic:1.2.3")
        implementation("org.apache.commons:commons-text:1.8")
    }
}
