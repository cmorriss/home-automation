import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.4.30"
    application
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    flatDir {
        dirs("libs")
    }
    mavenLocal()
    jcenter()
    maven("https://kotlin.bintray.com/ktor")
    maven("https://kotlin.bintray.com/kotlinx")
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}


val awsSdkVersion: String by project
val awsEventsVersion: String by project
val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val exposedVersion: String by project
val kotlinCoroutinesVersion: String by project
val junitVersion: String by project
val guiceVersion: String by project
val kotlinGuiceVersion: String by project
val awsIotDeviceSdkVersion: String by project
val cronUtilsVersion: String by project
val slf4jVersion: String by project

dependencies {
    compile(kotlin("stdlib"))
    compile(kotlin("reflect"))
    compile(platform("software.amazon.awssdk:bom:$awsSdkVersion"))
    compile("software.amazon.awssdk:eventbridge")
    compile("software.amazon.awssdk:cloudwatch")
    compile("software.amazon.awssdk:iotdataplane")
    compile("software.amazon.awssdk.iotdevicesdk:aws-iot-device-sdk:$awsIotDeviceSdkVersion")
    compile("com.google.inject:guice:$guiceVersion")
    compile("com.google.inject.extensions:guice-assistedinject:$guiceVersion")
    compile("dev.misfitlabs.kotlinguice4:kotlin-guice:$kotlinGuiceVersion")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    compile("org.apache.commons:commons-email:1.5")
    compile("javax.activation:javax.activation-api:1.2.0")
    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("io.ktor:ktor-client-apache:$ktorVersion")
    compile("io.ktor:ktor-client-core:$ktorVersion")
    compile("ch.qos.logback:logback-classic:$logbackVersion")
    compile("io.ktor:ktor-server-core:$ktorVersion")
    compile("io.ktor:ktor-client-gson:$ktorVersion")
    compile("io.ktor:ktor-client-logging-jvm:$ktorVersion")
    compile("io.ktor:ktor-locations:$ktorVersion")
    compile("io.ktor:ktor-server-sessions:$ktorVersion")
    compile("io.ktor:ktor-server-host-common:$ktorVersion")
    compile("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    compile("io.ktor:ktor-auth:$ktorVersion")
    compile("io.ktor:ktor-jackson:$ktorVersion")
    compile("io.micrometer:micrometer-registry-influx:latest.release")
    compile("com.h2database:h2:1.4.200")
    compile("joda-time:joda-time:2.10.8")
    compile("org.slf4j:slf4j-api:$slf4jVersion")
    compile("org.jetbrains.exposed:exposed:$exposedVersion")
    compile("com.cronutils:cron-utils:9.1.1")
    // cron-utils dependencies
    compile("javax.validation:validation-api:1.1.0.Final")
    compile("org.apache.commons:commons-lang3:3.11")

    testCompile("org.slf4j:slf4j-simple:$slf4jVersion")
    testCompile("io.ktor:ktor-server-tests:$ktorVersion")
    testCompile("io.ktor:ktor-server-test-host:$ktorVersion")
    testCompile("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    testCompile("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testCompile("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testCompile("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testCompile("io.mockk:mockk:1.10.2")
}

plugins.withId("kotlin") {
    configure<JavaPluginConvention> {
        sourceCompatibility = VERSION_1_8
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = VERSION_1_8.toString()
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
