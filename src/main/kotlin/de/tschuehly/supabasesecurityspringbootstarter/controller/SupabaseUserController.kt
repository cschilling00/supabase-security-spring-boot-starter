package de.tschuehly.supabasesecurityspringbootstarter.controller

import de.tschuehly.supabasesecurityspringbootstarter.exception.MissingCredentialsException
import de.tschuehly.supabasesecurityspringbootstarter.exception.SuccessfulRegistrationConfirmationEmailSent
import de.tschuehly.supabasesecurityspringbootstarter.service.SupabaseUserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.security.InvalidParameterException

@Controller
@RequestMapping("api/user")
class SupabaseUserController(
        val supabaseUserService: SupabaseUserService,
) {
    val logger: Logger = LoggerFactory.getLogger(SupabaseUserController::class.java)

    @PostMapping("/register")
    fun register(
        @RequestParam credentials: Map<String, String>,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val email = credentials["email"]
        val password = credentials["password"]
        if (email != null && password != null) {
            supabaseUserService.registerWithEmail(email.trim(), password.trim(), response)
        }else{
            throw MissingCredentialsException("User tried to register without providing email and password")
        }
    }

    @PostMapping("/login")
    fun login(
        @RequestParam credentials: Map<String, String>,
        response: HttpServletResponse,
        request: HttpServletRequest
    ) {
        val email = credentials["email"]
        val password = credentials["password"]
        if (email != null && password != null) {
            supabaseUserService.loginWithEmail(email.trim(), password.trim(), response)
        }else{
            throw MissingCredentialsException("User tried to login without providing email and password")
        }
    }

    @PostMapping("/jwt")
    fun authorizeWithJwtOrResetPassword(request: HttpServletRequest, response: HttpServletResponse) {
        supabaseUserService.authorizeWithJwtOrResetPassword(request, response)
    }

    @GetMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        supabaseUserService.logout(request, response)
    }

    @PutMapping("/setRoles")
    @ResponseBody
    fun setRoles(
        @RequestParam
        roles: List<String>?,
        request: HttpServletRequest,
        @RequestParam
        userId: String,
    ) {
        if (userId == "") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "UserId required")
        }
        supabaseUserService.setRolesWithRequest(request, userId, roles)
    }

    @PostMapping("/sendPasswordResetEmail")
    @ResponseBody
    fun sendPasswordResetEmail(
        @RequestParam
        email: String
    ) {
        logger.debug("User with the email $email requested a password reset")
        supabaseUserService.sendPasswordRecoveryEmail(email)
    }

    @PostMapping("/updatePassword")
    @ResponseBody
    fun updatePassword(
        request: HttpServletRequest,
        @RequestParam
        password: String
    ) {
        supabaseUserService.updatePassword(request, password)
    }
}
