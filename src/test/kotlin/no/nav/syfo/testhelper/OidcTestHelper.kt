package no.nav.syfo.testhelper

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.test.JwtTokenGenerator
import no.nav.syfo.api.auth.OIDCIssuer.AZURE
import no.nav.syfo.api.auth.OIDCIssuer.VEILEDERAZURE
import java.text.ParseException
import java.util.*

object OidcTestHelper {
    @Throws(ParseException::class)
    fun loggInnVeilederMedAzure(oidcRequestContextHolder: TokenValidationContextHolder, veilederIdent: String) {
        val claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"$veilederIdent\"}")
        val jwt = JwtTokenGenerator.createSignedJWT(claimsSet)
        settOIDCValidationContext(oidcRequestContextHolder, jwt, AZURE)
    }

    @JvmStatic
    @Throws(ParseException::class)
    fun logInVeilederWithAzure2(oidcRequestContextHolder: TokenValidationContextHolder, veilederIdent: String?) {
        val claimsSet = JWTClaimsSet.parse("{ }")
        val jwt = JwtTokenGenerator.createSignedJWT(claimsSet)
        settOIDCValidationContext(oidcRequestContextHolder, jwt, VEILEDERAZURE)
    }

    @JvmStatic
    fun loggUtAlle(oidcRequestContextHolder: TokenValidationContextHolder) {
        oidcRequestContextHolder.tokenValidationContext = null
    }

    private fun settOIDCValidationContext(tokenValidationContextHolder: TokenValidationContextHolder, jwt: SignedJWT, issuer: String) {
        val jwtToken = JwtToken(jwt.serialize())
        val issuerTokenMap: MutableMap<String, JwtToken> = HashMap()
        issuerTokenMap[issuer] = jwtToken
        val tokenValidationContext = TokenValidationContext(issuerTokenMap)
        tokenValidationContextHolder.tokenValidationContext = tokenValidationContext
    }
}
