package no.nav.syfo.geografisktilknytning

import io.micrometer.core.annotation.Timed
import no.nav.syfo.consumer.axsys.AxsysConsumer
import no.nav.syfo.consumer.axsys.AxsysEnhet
import no.nav.syfo.consumer.behandlendeenhet.BehandlendeEnhetConsumer
import no.nav.syfo.consumer.graphapi.GraphApiConsumer
import no.nav.syfo.consumer.norg2.NorgConsumer
import no.nav.syfo.consumer.pdl.*
import no.nav.syfo.domain.AdRoller
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class GeografiskTilgangService @Autowired constructor(
    private val adRoller: AdRoller,
    private val axsysConsumer: AxsysConsumer,
    private val behandlendeEnhetConsumer: BehandlendeEnhetConsumer,
    private val graphApiConsumer: GraphApiConsumer,
    private val norgConsumer: NorgConsumer,
    private val pdlConsumer: PdlConsumer
) {
    @Timed("syfotilgangskontroll_harGeografiskTilgang", histogram = true)
    fun harGeografiskTilgang(veilederId: String, personFnr: String): Boolean {
        if (harNasjonalTilgang(veilederId)) {
            return true
        }
        val geografiskTilknytning = pdlConsumer.geografiskTilknytning(personFnr)
        val navKontorForGT = getNavKontorForGT(personFnr, geografiskTilknytning)
        val veiledersEnheter = axsysConsumer.enheter(veilederId)
            .stream()
            .map(AxsysEnhet::enhetId)
            .collect(Collectors.toList())
        return (
            harLokalTilgangTilBrukersEnhet(navKontorForGT, veiledersEnheter) ||
                harRegionalTilgangTilBrukersEnhet(navKontorForGT, veiledersEnheter, veilederId)
            )
    }

    private fun harNasjonalTilgang(veilederId: String): Boolean {
        return (
            graphApiConsumer.hasAccess(veilederId, adRoller.NASJONAL) ||
                graphApiConsumer.hasAccess(veilederId, adRoller.UTVIDBAR_TIL_NASJONAL)
            )
    }

    private fun harLokalTilgangTilBrukersEnhet(navKontorForGT: String, veiledersEnheter: List<String>): Boolean {
        return veiledersEnheter.contains(navKontorForGT)
    }

    private fun harRegionalTilgang(veilederId: String): Boolean {
        return (
            graphApiConsumer.hasAccess(veilederId, adRoller.REGIONAL) ||
                graphApiConsumer.hasAccess(veilederId, adRoller.UTVIDBAR_TIL_REGIONAL)
            )
    }

    private fun harRegionalTilgangTilBrukersEnhet(navKontorForGT: String, veiledersEnheter: List<String>, veilederId: String): Boolean {
        val veiledersOverordnedeEnheter = veiledersEnheter.map { enhetNr: String ->
            norgConsumer.getOverordnetEnhetListForNAVKontor(enhetNr)
        }.flatten()

        return harRegionalTilgang(veilederId) && norgConsumer.getOverordnetEnhetListForNAVKontor(navKontorForGT)
            .any { overordnetEnhet: String -> veiledersOverordnedeEnheter.contains(overordnetEnhet) }
    }

    private fun getNavKontorForGT(personFnr: String, geografiskTilknytning: GeografiskTilknytning): String {
        return if (isGeografiskTilknytningUtlandOrNorgeWithoutGT(geografiskTilknytning)) behandlendeEnhetConsumer.getBehandlendeEnhet(personFnr, null).enhetId else norgConsumer.getNAVKontorForGT(geografiskTilknytning)
    }

    private fun isGeografiskTilknytningUtlandOrNorgeWithoutGT(geografiskTilknytning: GeografiskTilknytning): Boolean {
        return geografiskTilknytning.type == GeografiskTilknytningType.UTLAND || geografiskTilknytning.value == null
    }
}
