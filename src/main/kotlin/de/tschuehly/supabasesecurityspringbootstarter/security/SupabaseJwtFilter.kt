package de.tschuehly.supabasesecurityspringbootstarter.security

import com.auth0.jwt.exceptions.TokenExpiredException
import de.tschuehly.supabasesecurityspringbootstarter.service.SupabaseUserServiceGoTrueImpl
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SupabaseJwtFilter(
    val supabaseUserService: SupabaseUserServiceGoTrueImpl
) : OncePerRequestFilter() {
    val logger: Logger = LoggerFactory.getLogger(SupabaseJwtFilter::class.java)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val jwtCookie = request.cookies?.find { it.name == "JWT" }
        if (jwtCookie != null) {
            try {
                SecurityContextHolder.getContext().authentication =
                    supabaseUserService.getAuthenticationToken(jwtCookie.value).also {
                        logger.debug("Set authentication to $it")
                    }
            } catch (e: TokenExpiredException) {

                jwtCookie.maxAge = 0
                response.addCookie(jwtCookie)
            }
        }
        filterChain.doFilter(request, response)
    }

}
