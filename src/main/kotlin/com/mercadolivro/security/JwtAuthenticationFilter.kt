package com.mercadolivro.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.mercadolivro.controller.request.CredentialRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.IOException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.jvm.Throws

class JwtAuthenticationFilter(
        authenticationManager: AuthenticationManager,
        private val jwtUtil: JwtUtil
) : UsernamePasswordAuthenticationFilter(authenticationManager) {

    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        return try {
            val credential = ObjectMapper().readValue(request.inputStream, CredentialRequest::class.java)
            val authToken = UsernamePasswordAuthenticationToken(credential.email, credential.password, ArrayList())
            authenticationManager.authenticate(authToken)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class, ServletException::class)
    override fun successfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain,
                                          authResult: Authentication) {
        val username: String = (authResult.principal as UserSecurity).username
        val token: String = jwtUtil.generateToken(username)!!
        response.addHeader("Authorization", "Bearer $token")
    }

}