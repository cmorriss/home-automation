@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey

import io.ktor.client.features.logging.*
import io.ktor.config.*
import io.morrissey.routes.*
import org.slf4j.event.*
import java.io.*
import java.nio.file.*
import java.util.*
import kotlin.reflect.*

class HomeServerConfig(val appConfig: ApplicationConfig? = null, val overrides: Map<String, String> = emptyMap()) {
    companion object {
        const val APP_CONFIG_BASE = "ktor.application."
    }

    val configDir: File by AppConfigProperty("/etc/home-automation-server") { File(it) }
    val libDir: String by AppConfigProperty("/usr/lib/home-automation-server") { it }
    val dbFile: File by AppConfigProperty("${configDir.absolutePath}/db/prod") { File(it) }
    val appContentDir: String by AppConfigProperty("$libDir/app") { it }
    val logLevel: Level by AppConfigProperty("DEBUG") { Level.valueOf(it) }
    val requestLogLevel: LogLevel by AppConfigProperty("INFO") { LogLevel.valueOf(it) }
    val nodeMaxRetry: Int by AppConfigProperty("3") { it.toInt() }
    val nodeRetryWait: Long by AppConfigProperty("5_000") { it.toLong() }
    val authenticate: Boolean by AppConfigProperty("true") { it.toBoolean() }
    val unauthenticatedPrincipal: String by AppConfigProperty("anonymous") { it }
    val sessionSignSecret: String by SecureConfigProperty()
    val clientId: String by SecureConfigProperty()
    val clientSecret: String by SecureConfigProperty()
    val authorizedEmails: String by SecureConfigProperty()
    val adminEmails: String by SecureConfigProperty()
    val senderEmail: String by SecureConfigProperty()
    val senderPassword: String by SecureConfigProperty()

    init {
        log.info(
            """Server config initialized to:
            |  configDir = $configDir
            |  dbFile = $dbFile
            |  logLevel = $logLevel
            |  requestLogLevel = $requestLogLevel
        """.trimMargin()
        )
    }

    inner class AppConfigProperty<T : Any>(private val default: String, val converter: (String) -> T) {
        operator fun getValue(config: HomeServerConfig, property: KProperty<*>): T {
            val propertyName = APP_CONFIG_BASE + property.name
            val propertyValue =
                    overrides[property.name] ?: appConfig?.propertyOrNull(propertyName)?.getString() ?: default
            return converter(propertyValue)
        }
    }

    inner class SecureConfigProperty {
        private lateinit var secureProperties: Properties

        operator fun getValue(config: HomeServerConfig, property: KProperty<*>): String {
            if (!::secureProperties.isInitialized) {
                val securePropertiesPath =
                        Paths.get(config.configDir.absolutePath, "secure.properties").normalize().toString()
                secureProperties = Properties().apply {
                    if (File(securePropertiesPath).exists()) {
                        load(FileReader(securePropertiesPath))
                    }
                }
            }
            return overrides[property.name] ?: secureProperties.getProperty(property.name)
                    ?: throw ServiceConfigurationError("The secure properties file is missing the key ${property.name}")
        }
    }
}