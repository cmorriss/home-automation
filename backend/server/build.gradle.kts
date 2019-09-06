plugins {
    java
    kotlin("jvm")
    kotlin("kapt")
    application
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val requeryVersion: String by project
val kotlinCoroutinesVersion: String by project

dependencies {
    compile(kotlin("stdlib"))
    compile(kotlin("reflect"))
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$kotlinCoroutinesVersion")
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
    compile("io.requery:requery:$requeryVersion")
    compile("io.requery:requery-kotlin:$requeryVersion")
    compile("com.h2database:h2:1.4.199")
    compile("joda-time:joda-time:2.10.3")
    kapt("io.requery:requery-processor:$requeryVersion")
    testCompile("io.ktor:ktor-server-tests:$ktorVersion")
    testCompile("io.ktor:ktor-server-test-host:$ktorVersion")
    testCompile("io.ktor:ktor-client-mock:$ktorVersion")
    testCompile("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    testCompile("io.mockk:mockk:1.9")
}
