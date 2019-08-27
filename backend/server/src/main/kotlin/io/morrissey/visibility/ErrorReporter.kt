package io.morrissey.visibility

import io.ktor.client.HttpClient
import io.morrissey.HomeServerConfig
import io.morrissey.model.IotLocation
import io.morrissey.routes.PhysicalSwitchResult
import io.morrissey.routes.getPhysicalSwitches
import io.morrissey.routes.log
import io.morrissey.schedule.Scheduler
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.HtmlEmail
import java.lang.Thread.sleep
import java.util.*

class ErrorReporter(
    reportTimer: Timer,
    private val emailFactory: EmailFactory,
    private val httpClient: HttpClient,
    private val homeServerConfig: HomeServerConfig
) {
    private val sentNotifications = mutableMapOf<IotLocation, String>()

    init {
        reportTimer.scheduleAtFixedRate(CheckSwitchControl(), 0L, Scheduler.FIVE_MINUTES_IN_MILLIS)
    }

    inner class CheckSwitchControl : TimerTask() {
        override fun run() {
            IotLocation.values().forEach {
                verifyAndNotify(it)
            }
        }

        private fun verifyAndNotify(loc: IotLocation) {
            val result = verify(loc)
            if (!result.success && !notified(loc, result)) {
                notify(loc, result.failureReason)
            } else if (result.success && sentNotifications.containsKey(loc)) {
                log.info("Location ${loc.name} is back online.")
                sentNotifications.remove(loc)
            }
        }

        private fun notified(loc: IotLocation, result: VerificationResult): Boolean {
            return sentNotifications.any { entry ->
                entry.key == loc && entry.value == result.failureReason
            }
        }

        private fun verify(loc: IotLocation, tries: Int = 1): VerificationResult {
            return when (val result = getPhysicalSwitches(httpClient, loc)) {
                is PhysicalSwitchResult.FailureResult -> if (tries == homeServerConfig.nodeMaxRetry) {
                    VerificationResult(false, result.statusMessage)
                } else {
                    sleep(homeServerConfig.nodeRetryWait)
                    verify(loc, tries + 1)
                }
                is PhysicalSwitchResult.SuccessResult -> VerificationResult(true, "OK")
            }
        }

        private fun notify(loc: IotLocation, reason: String) {
            log.error("Sending email notifications for location ${loc.name} with reason $reason")
            sentNotifications[loc] = reason
            homeServerConfig.adminEmails.split(",").forEach { recipientEmail ->
                val email = emailFactory.buildEmail()
                email.hostName = "smtp.googlemail.com"
                email.setSmtpPort(465)
                email.setAuthenticator(
                    DefaultAuthenticator(
                        homeServerConfig.senderEmail,
                        homeServerConfig.senderPassword
                    )
                )
                email.isSSLOnConnect = true
                email.setFrom(homeServerConfig.senderEmail)
                email.addTo(recipientEmail)
                email.subject = "Warning: Error contacting ${loc.name} switch control"
                email.setHtmlMsg(
                    """<html><h1>${loc.name} Switch Control Connection Error</h1>
                |Home server received the following error while verifying the connection:<br/>
                |$reason
                |</html>
            """.trimMargin()
                )
                email.send()
            }
        }
    }

    data class VerificationResult(val success: Boolean, val failureReason: String)
}

class EmailFactory {
    fun buildEmail(): HtmlEmail {
        return HtmlEmail()
    }
}