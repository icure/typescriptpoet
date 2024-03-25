import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `java-library`
  jacoco
  `maven-publish`
  signing

  kotlin("jvm") version "1.9.22"
  id("org.jetbrains.dokka") version "1.9.20"

  id("org.jmailen.kotlinter") version "4.3.0"
  id("com.github.breadmoirai.github-release") version "2.2.12"
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
// DOCS
//

tasks {
  dokkaHtml {
    outputDirectory.set(file("$buildDir/javadoc/$releaseVersion"))
  }

  javadoc {
    dependsOn(dokkaHtml)
  }
}

//
// PUBLISHING
//

publishing {

  publications {

    create<MavenPublication>("library") {
      from(components["java"])

      pom {
        name.set("TypeScript Poet")
        description.set("TypeScriptPoet is a Kotlin and Java API for generating .ts source files.")
        url.set("https://github.com/voize-gmbh/typescriptpoet")

        scm {
            url.set("https://github.com/voize-gmbh/reakt-native-toolkit")
        }

        licenses {
          license {
            name.set("Apache-2.0")
            url.set("https://opensource.org/licenses/Apache-2.0")
          }
        }
        developers {
          developer {
            id.set("LeonKiefer")
            name.set("Leon Kiefer")
            email.set("leon@voize.de")
          }
          developer {
            id.set("ErikZiegler")
            name.set("Erik Ziegler")
            email.set("erik@voize.de")
          }
        }
      }
    }
  }

  repositories {

    maven {
      name = "MavenCentral"
      val snapshotUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
      val releaseUrl = "https://s01.oss.sonatype.org/service/local/"
      url = uri(if (isSnapshot) snapshotUrl else releaseUrl)

      credentials {
        username = project.findProperty("ossrhUsername")?.toString()
        password = project.findProperty("ossrhPassword")?.toString()
      }
    }

  }

}

signing {
  if (!hasProperty("signing.keyId")) {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
  }
  sign(publishing.publications["library"])
}

tasks.withType<Sign>().configureEach {
  onlyIf { !isSnapshot }
}


//
// RELEASING
//

githubRelease {
  owner("outfoxx")
  repo(name)
  tagName("v$releaseVersion")
  targetCommitish("main")
  releaseName("🎉 $releaseVersion Release")
  draft(true)
  prerelease(!releaseVersion.matches("""^\d+\.\d+\.\d+$""".toRegex()))
  releaseAssets(
    files("$buildDir/libs/${name}-${releaseVersion}*.jar")
  )
  overwrite(true)
  token(project.findProperty("github.token") as String? ?: System.getenv("GITHUB_TOKEN"))
}

tasks {

  register("publishMavenRelease") {
    dependsOn(
      "publishAllPublicationsToMavenCentralRepository"
    )
  }

  register("publishRelease") {
    dependsOn(
      "publishMavenRelease",
      "githubRelease"
    )
  }

}
