import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50" apply false
}

subprojects {
    afterEvaluate {
        plugins.withId("kotlin") {
            configure<JavaPluginConvention> {
                sourceCompatibility = JavaVersion.VERSION_1_8
            }

            tasks.withType<KotlinCompile> {
                kotlinOptions {
                    jvmTarget = VERSION_1_8.toString()
                }
            }
        }
    }
}

allprojects {
    repositories {
        mavenLocal()
        jcenter()
        maven("https://kotlin.bintray.com/ktor")
        maven("https://kotlin.bintray.com/kotlinx")
    }
}
