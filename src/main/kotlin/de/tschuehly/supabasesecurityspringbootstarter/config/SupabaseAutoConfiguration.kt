package de.tschuehly.supabasesecurityspringbootstarter.config

import io.supabase.gotrue.GoTrueClient
import io.supabase.gotrue.types.GoTrueTokenResponse
import de.tschuehly.supabasesecurityspringbootstarter.controller.SupabaseUserController
import de.tschuehly.supabasesecurityspringbootstarter.security.SupabaseSecurityConfig
import de.tschuehly.supabasesecurityspringbootstarter.service.SupabaseUserService
import de.tschuehly.supabasesecurityspringbootstarter.types.SupabaseUser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.*

@Configuration
@ConditionalOnProperty(prefix = "supabase", name = ["projectId"])
@ComponentScan("de.tschuehly.supabasesecurityspringbootstarter")
@EnableConfigurationProperties(de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseProperties::class)
@Import(SupabaseSecurityConfig::class)
@PropertySource("classpath:application-supabase.properties")
class SupabaseAutoConfiguration(
    val supabaseProperties: de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseProperties
) {
    val logger: Logger = LoggerFactory.getLogger(de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseAutoConfiguration::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun supabaseService(supabaseGoTrueClient: GoTrueClient<SupabaseUser, GoTrueTokenResponse>): SupabaseUserService {
        logger.debug("Registering the SupabaseUserService")
        return SupabaseUserService(supabaseProperties, supabaseGoTrueClient)
    }

    @Bean
    @ConditionalOnMissingBean
    fun supabaseController(supabaseUserService: SupabaseUserService): SupabaseUserController {
        logger.debug("Registering the SupabaseUserController")
        return SupabaseUserController(supabaseUserService)
    }

    @Bean
    @ConditionalOnMissingBean
    fun supabaseGoTrueClient(supabaseProperties: de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseProperties): GoTrueClient<SupabaseUser, GoTrueTokenResponse> {
        logger.debug("Registering the supabaseGoTrueClient")
        return GoTrueClient.customApacheJacksonGoTrueClient(
            url = "https://${supabaseProperties.projectId}.supabase.co/auth/v1",
            headers = mapOf("apiKey" to supabaseProperties.anonKey!!)
        )
    }


}
