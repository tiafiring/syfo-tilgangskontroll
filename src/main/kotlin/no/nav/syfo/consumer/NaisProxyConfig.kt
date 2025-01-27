package no.nav.syfo.consumer

import org.apache.http.*
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.DefaultProxyRoutePlanner
import org.apache.http.protocol.HttpContext
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

class NaisProxyConfig : RestTemplateCustomizer {
    override fun customize(restTemplate: RestTemplate) {
        val proxy = HttpHost(WEBPROXY_NAIS_HOST, WEBPROXY_NAIS_PORT)
        val client: HttpClient = HttpClientBuilder.create()
            .setRoutePlanner(object : DefaultProxyRoutePlanner(proxy) {
                @Throws(HttpException::class)
                public override fun determineProxy(
                    target: HttpHost,
                    request: HttpRequest,
                    context: HttpContext
                ): HttpHost? {
                    return if (target.hostName.contains("microsoft")) {
                        super.determineProxy(target, request, context)
                    } else null
                }
            })
            .build()
        restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory(client)
    }

    companion object {
        const val WEBPROXY_NAIS_HOST = "webproxy-nais.nav.no"
        const val WEBPROXY_NAIS_PORT = 8088
    }
}
