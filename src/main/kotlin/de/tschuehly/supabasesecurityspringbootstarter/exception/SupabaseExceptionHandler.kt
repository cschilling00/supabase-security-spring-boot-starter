package de.tschuehly.supabasesecurityspringbootstarter.exception

import de.tschuehly.supabasesecurityspringbootstarter.exception.InvalidLoginCredentialsException
import de.tschuehly.supabasesecurityspringbootstarter.exception.MissingCredentialsException
import de.tschuehly.supabasesecurityspringbootstarter.exception.SuccessfulRegistrationConfirmationEmailSent
import de.tschuehly.supabasesecurityspringbootstarter.exception.UserNeedsToConfirmEmailBeforeLoginException
import org.springframework.web.bind.annotation.ExceptionHandler

interface SupabaseExceptionHandler {
    @ExceptionHandler(MissingCredentialsException::class)
    fun handleMissingCredentialsException(exception: MissingCredentialsException): String

    @ExceptionHandler(InvalidLoginCredentialsException::class)
    fun handleInvalidLoginCredentialsException(exception: InvalidLoginCredentialsException): String

    @ExceptionHandler(UserNeedsToConfirmEmailBeforeLoginException::class)
    fun handleUserNeedsToConfirmEmail(exception: UserNeedsToConfirmEmailBeforeLoginException): String

    @ExceptionHandler(SuccessfulRegistrationConfirmationEmailSent::class)
    fun handleSuccessfulRegistration(exception: SuccessfulRegistrationConfirmationEmailSent): String

    @ExceptionHandler(PasswordRecoveryEmailSent::class)
    fun handlePasswordRecoveryEmailSent(exception: PasswordRecoveryEmailSent): String

    @ExceptionHandler(SuccessfulPasswordUpdate::class)
    fun handleSuccessfulPasswordUpdate(exception: SuccessfulPasswordUpdate): String
}