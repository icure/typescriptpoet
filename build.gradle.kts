import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  jacoco

  kotlin("jvm") version "1.9.22"
  id("org.jetbrains.dokka") version "1.9.20"

  id("org.jmailen.kotlinter") version "4.3.0"

  id("convention.publication")
}


val releaseVersion: String by project
val isSnapshot = releaseVersion.endsWith("SNAPSHOT")


group = "de.voize"
version = releaseVersion
description = "A Kotlin/Java API for generating .ts source files."


//
// DEPENDENCIES
//

// Versions

val guavaVersion = "22.0"
val junitJupiterVersion = "5.6.2"
val hamcrestVersion = "1.3"

repositories {
  mavenCentral()
}

dependencies {

  //
  // LANGUAGES
  //

  // kotlin
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  //
  // MISCELLANEOUS
  //

  implementation("com.google.guava:guava:$guavaVersion")

  //
  // TESTING
  //

  // junit
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
  testImplementation("org.hamcrest:hamcrest-all:$hamcrestVersion")

}


//
// COMPILE
//

val javaVersion = JavaVersion.VERSION_1_8

java {
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion

  withSourcesJar()
  withJavadocJar()
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "$javaVersion"
    }
  }
}


//
// TEST
//

jacoco {
  toolVersion = "0.8.11"
}

tasks {
  test {
    useJUnitPlatform()

    finalizedBy(jacocoTestReport)
    jacoco {}
  }

  jacocoTestReport {
    dependsOn(test)
  }
}

//
// PUBLISHING
//

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      pom {
        name.set("TypeScript Poet")
        description.set("TypeScriptPoet is a Kotlin and Java API for generating .ts source files.")
      }
    }
  }
}

tasks.withType<Sign>().configureEach {
  onlyIf { !isSnapshot }
}
