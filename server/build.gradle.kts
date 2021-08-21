import org.gradle.api.JavaVersion.VERSION_14
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.5.20"
    java
    kotlin("jvm") version kotlinVersion
    application
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
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

val kotlinVersion = "1.5.21"
val awsSdkVersion = "2.15.77"
val awsXrayVersion = "2.9.0"
val ktorVersion = "1.6.2"
val logbackVersion = "1.2.5"
val exposedVersion = "0.17.13"
val kotlinCoroutinesVersion = "1.5.1-native-mt"
val junitVersion = "5.7.2"
val awsIotDeviceSdkVersion = "1.4.2"
val slf4jVersion = "1.7.32"
val koinVersion = "2.2.2"

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.2.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.21")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.21")

    // KTor
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common:$ktorVersion")
    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("org.apache.commons:commons-email:1.5")
    implementation("javax.activation:javax.activation-api:1.2.0")

    // H2 DB
    implementation("com.h2database:h2:1.4.200")

    // AWS
    implementation(platform("software.amazon.awssdk:bom:$awsSdkVersion"))
    implementation("software.amazon.awssdk:eventbridge:2.17.14")
    implementation("software.amazon.awssdk:cloudwatch:2.17.14")
    implementation("software.amazon.awssdk:iotdataplane:2.17.14")
    implementation("software.amazon.awssdk.iotdevicesdk:aws-iot-device-sdk:$awsIotDeviceSdkVersion")

    // AWS Xray
    implementation(platform("com.amazonaws:aws-xray-recorder-sdk-bom:$awsXrayVersion"))
    implementation("com.amazonaws:aws-xray-recorder-sdk-core:2.9.1")
    implementation("com.amazonaws:aws-xray-recorder-sdk-aws-sdk:2.9.1")
    implementation("com.amazonaws:aws-xray-recorder-sdk-aws-sdk-instrumentor:2.9.1")
    implementation("com.amazonaws:aws-xray-recorder-sdk-apache-http:2.9.1")

    // Utils
    implementation("joda-time:joda-time:2.10.10")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.jetbrains.exposed:exposed:$exposedVersion")
    implementation("com.cronutils:cron-utils:9.1.5")
    implementation("org.koin:koin-ktor:$koinVersion")
    implementation("org.koin:koin-logger-slf4j:$koinVersion")
    // cron-utils dependencies
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.apache.commons:commons-lang3:3.12.0")

    // Test
    testImplementation("org.slf4j:slf4j-simple:$slf4jVersion")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("org.koin:koin-test:$koinVersion")
    testImplementation("org.koin:koin-test-junit5:$koinVersion")

}

plugins.withId("kotlin") {
    configure<JavaPluginConvention> {
        sourceCompatibility = VERSION_14
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = VERSION_14.toString()
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
