package de.tschuehly.supabasesecurityspringbootstarter.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.supabase.gotrue.GoTrueClient
import io.supabase.gotrue.http.GoTrueHttpException
import io.supabase.gotrue.types.GoTrueTokenResponse
import io.supabase.gotrue.types.GoTrueUserAttributes
import de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseProperties
import de.tschuehly.supabasesecurityspringbootstarter.exception.*
import de.tschuehly.supabasesecurityspringbootstarter.security.SupabaseAuthenticationToken
import de.tschuehly.supabasesecurityspringbootstarter.types.SupabaseUser
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class SupabaseUserService(
    val supabaseProperties: SupabaseProperties,
    val supabaseGoTrueClient: GoTrueClient<SupabaseUser, GoTrueTokenResponse>
) {
    val logger: Logger = LoggerFactory.getLogger(SupabaseUserService::class.java)
    fun registerWithEmail(email: String, password: String, response: HttpServletResponse): SupabaseUser {
        try {
            return supabaseGoTrueClient.signUpWithEmail(email, password)
        } catch (e: GoTrueHttpException) {
            if (e.data?.contains("User already registered") == true) {
                throw UserAlreadyRegisteredException("User: ${email} already registered", e)
            } else {
                logger.error(e.data)
                throw e
            }
        }

    }

    fun login(
        username: String, password: String, response: HttpServletResponse
    ): HttpServletResponse {
        try {
            val resp = supabaseGoTrueClient.signInWithEmail(username, password)
            setCookies(response, resp.accessToken)
        } catch (e: GoTrueHttpException) {
            if (e.data?.contains("Invalid login credentials") == true) {
                val msg = "$username either does not exist or has tried to login with the wrong password"
                logger.debug(msg)
                throw InvalidLoginCredentialsException(
                    msg,
                    e
                )
            } else if (e.data?.contains("Email not confirmed") == true) {
                val msg = "$username needs to confirm email before he can login"
                logger.debug(msg)
                throw UserNeedsToConfirmEmailBeforeLoginException(msg)
            } else {
                logger.error(e.data)
                throw e
            }
        }
        return response
    }

    fun authorizeWithJwtOrResetPassword(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): HttpServletResponse {
        val header: String? = request.getHeader("HX-Current-URL")
        if (header != null) {
            val accessToken = header.substringBefore("&").substringAfter("#access_token=")
            val authenticationToken = getAuthenticationToken(accessToken)
            setCookies(response, accessToken)
            if (header.contains("type=recovery")) {
                logger.debug("User: ${authenticationToken.getSupabaseUser().email} is trying to reset his password")
                response.setHeader("HX-Redirect", supabaseProperties.passwordRecoveryPage)
            }
        }
        return response
    }

    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        SecurityContextHolder.getContext().authentication = null
        request.cookies?.find { it.name == "JWT" }?.let {
            var cookieString = "JWT=${it.value}; HttpOnly; Path=/;Max-Age=0;"
            if (supabaseProperties.sslOnly) {
                cookieString += "Secure;"
            }
            response.setHeader("Set-Cookie", cookieString)
            response.setHeader("HX-Redirect", "/") // TODO: Introduce Redirect Header or HTXM / JSON Switch
        }
    }

    fun setRolesWithRequest(request: HttpServletRequest, userId: String, roles: List<String>?) {
        request.cookies?.find { it.name == "JWT" }?.let {
            setRoles(it.value,userId,roles)
        }
    }

    fun setRoles(serviceRoleJWT: String, userId: String,roles: List<String>?){
        val roleArray = roles ?: listOf()
        supabaseGoTrueClient.updateUserAppMetadata(serviceRoleJWT, userId, mapOf("roles" to roleArray))
        logger.debug("The roles of the user with id $userId were updated to $roleArray")
    }


    private fun setCookies(
        response: HttpServletResponse,
        accessToken: String
    ) {
        response.addCookie(Cookie("JWT", accessToken).also {
            it.secure = supabaseProperties.sslOnly
            it.isHttpOnly = true
            it.path = "/"
            it.maxAge = 6000
        })
        response.setHeader("HX-Redirect", supabaseProperties.successfulLoginRedirectPage)
    }

    fun getAuthenticationToken(jwt: String): SupabaseAuthenticationToken {
        val jwtClaims = JWT
            .require(Algorithm.HMAC256(supabaseProperties.jwtSecret)).build().verify(jwt).claims

        return SupabaseAuthenticationToken(
            SupabaseUser(jwtClaims)
        )
    }

    fun sendPasswordRecoveryEmail(email: String) {
        supabaseGoTrueClient.resetPasswordForEmail(email)
        throw PasswordRecoveryEmailSent("User with $email has requested a password recovery email")
    }

    fun updatePassword(request: HttpServletRequest, password: String) {
        request.cookies?.find { it.name == "JWT" }?.let { cookie ->
            supabaseGoTrueClient.updateUser(
                cookie.value,
                attributes = GoTrueUserAttributes(
                    password = password
                )
            )
            val user = getAuthenticationToken(jwt = cookie.value).getSupabaseUser()
            val msg = "User with the mail: ${user.email} updated his password successfully"
            logger.debug(msg)
            throw SuccessfulPasswordUpdate(msg)
        }
    }
}
