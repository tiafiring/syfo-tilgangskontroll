package no.nav.syfo.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class OrganisasjonRessursEnhetService {

    @Autowired
    private OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1;

    public List<String> hentVeiledersEnheter(String veilederId) {
        try {
            return organisasjonRessursEnhetV1
                    .hentEnhetListe(new WSHentEnhetListeRequest().withRessursId(veilederId))
                    .getEnhetListe().stream()
                    .map(WSEnhet::getEnhetId)
                    .collect(toList());
        } catch (HentEnhetListeUgyldigInput | HentEnhetListeRessursIkkeFunnet e) {
            log.error("Feil ved henting av NAV Ressurs sin enhetliste.", e);
            return emptyList();
        }
    }

    public boolean harTilgangTilEnhet(String veilederId, String navEnhet) {
        return hentVeiledersEnheter(veilederId)
                .stream()
                .anyMatch(enhet -> enhet.equals(navEnhet));
    }
}
