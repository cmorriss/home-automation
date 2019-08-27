plugins {
    java
    kotlin("jvm")
    application
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

val ktorVersion: String by project
val logbackVersion: String by project
val exposedVersion: String by project

dependencies {
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
    compile("org.jetbrains.exposed:exposed:$exposedVersion") {
        exclude(module = "log4j")
        exclude(module = "slf4j-log4j12")
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-reflect")
    }
    testCompile("io.ktor:ktor-server-tests:$ktorVersion")
    testCompile("io.ktor:ktor-server-test-host:$ktorVersion")
    testCompile("io.ktor:ktor-client-mock:$ktorVersion")
    testCompile("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    testCompile("io.mockk:mockk:1.9")
}