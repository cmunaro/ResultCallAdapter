plugins {
    kotlin("multiplatform") version "1.6.10"
}

group = "io.github.munez07"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.squareup.retrofit2:retrofit:2.7.0")
                implementation("com.squareup.retrofit2:converter-gson:2.7.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("junit:junit:4.13.1")
                implementation("com.google.truth:truth:1.1.3")
                implementation("com.squareup.okhttp3:mockwebserver:4.9.3")
                implementation("io.mockk:mockk:1.12.1")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
            }
        }
        val jvmTest by getting
    }
}
