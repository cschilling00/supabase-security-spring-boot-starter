package de.tschuehly.supabasesecurityspringbootstarter.application.advice

import org.slf4j.LoggerFactory


import de.tschuehly.supabasesecurityspringbootstarter.exception.*
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionController() : SupabaseExceptionHandler {
    val logger = LoggerFactory.getLogger(ExceptionController::class.java)

    override fun handleMissingCredentialsException(exception: MissingCredentialsException): String {
        logger.debug(exception.message)
        return "index"
    }

    override fun handleInvalidLoginCredentialsException(exception: InvalidLoginCredentialsException): String {
        logger.debug(exception.message)
        return "index"
    }

    override fun handleUserNeedsToConfirmEmail(exception: UserNeedsToConfirmEmailBeforeLoginException): String {
        logger.debug(exception.message)
        return "index"
    }

    override fun handleSuccessfulRegistration(exception: SuccessfulRegistrationConfirmationEmailSent): String {
        logger.debug(exception.message)
        return "index"
    }

    override fun handlePasswordRecoveryEmailSent(exception: PasswordRecoveryEmailSent): String {
        logger.debug(exception.message)
        return "index"
    }

    override fun handleSuccessfulPasswordUpdate(exception: SuccessfulPasswordUpdate): String {
        logger.debug(exception.message)
        return "index"
    }
}
