plugins {
    kotlin("jvm")
    application
}

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("ch.qos.logback:logback-classic:$logbackVersion")
    compile("io.ktor:ktor-server-core:$ktorVersion")
    compile("io.ktor:ktor-locations:$ktorVersion")
    compile("io.ktor:ktor-jackson:$ktorVersion")
    compile("io.ktor:ktor-client-core:$ktorVersion")
    compile("io.ktor:ktor-client-core-jvm:$ktorVersion")
    compile("io.ktor:ktor-client-apache:$ktorVersion")
    compile("io.ktor:ktor-client-json-jvm:$ktorVersion")
    compile("io.ktor:ktor-client-gson:$ktorVersion")
    compile("io.ktor:ktor-client-logging-jvm:$ktorVersion")
    compile("com.pi4j:pi4j-core:1.2")
    testCompile("io.ktor:ktor-server-tests:$ktorVersion")
}