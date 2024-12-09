import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  jacoco

  kotlin("jvm") version "1.9.25"

  id("org.jmailen.kotlinter") version "4.3.0"
  `maven-publish`

}


val releaseVersion: String by project
val isSnapshot = releaseVersion.endsWith("SNAPSHOT")
val repoUsername: String by project
val repoPassword: String by project
val mavenReleasesRepository: String by project


group = "io.icure"
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

val javaVersion = JavaVersion.VERSION_21

java {
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion

  withSourcesJar()
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
      artifactId = "typescript-poet"
      from(components["java"])
    }
  }
  repositories {
    maven {
      name = "Taktik"
      url = uri(mavenReleasesRepository)
      credentials {
        username = repoUsername
        password = repoPassword
      }
    }
  }

}

tasks.withType<Sign>().configureEach {
  onlyIf { !isSnapshot }
}
