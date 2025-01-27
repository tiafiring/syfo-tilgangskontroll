package no.nav.syfo.consumer.skjermedepersoner

import no.nav.syfo.cache.CacheConfig
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import javax.inject.Inject

@Service
class SkjermedePersonerPipConsumer @Inject constructor(
    private val metric: Metric,
    private val restTemplate: RestTemplate,
) {
    @Cacheable(
        cacheNames = [CacheConfig.CACHENAME_EGENANSATT],
        key = "#personIdent",
        condition = "#personIdent != null"
    )
    fun erSkjermet(personIdent: String): Boolean {
        try {
            val response = restTemplate.exchange(
                getSkjermedePersonerPipUrl(personIdent),
                HttpMethod.GET,
                entity(),
                Boolean::class.java
            )
            val skjermedePersonerResponse = response.body!!
            metric.countEvent(METRIC_CALL_SKJERMEDE_PERSONER_PIP_SUCCESS)
            return skjermedePersonerResponse
        } catch (e: RestClientResponseException) {
            metric.countEvent(METRIC_CALL_SKJERMEDE_PERSONER_PIP_FAIL)
            val message =
                "Call to get response from Skjermede Person failed with status: ${e.rawStatusCode} and message: ${e.responseBodyAsString}"
            LOG.error(message)
            throw e
        }
    }

    private fun getSkjermedePersonerPipUrl(personIdent: String): String {
        return "http://skjermede-personer-pip.nom.svc.nais.local/skjermet?personident=$personIdent"
    }

    private fun entity(): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        headers[NAV_CALL_ID_HEADER] = createCallId()
        return HttpEntity(headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SkjermedePersonerPipConsumer::class.java)

        private const val METRIC_CALL_SKJERMEDE_PERSONER_PIP_SUCCESS = "call_skjermede_person_pip_success"
        private const val METRIC_CALL_SKJERMEDE_PERSONER_PIP_FAIL = "call_skjermede_person_pip_fail"
    }
}
