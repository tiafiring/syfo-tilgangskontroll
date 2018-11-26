package no.nav.syfo.services;

import no.nav.syfo.domain.AdRoller;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeografiskTilgangServiceTest {

    private static final String VEILEDER_UID = "Z999999";
    private static final String BRUKER_FNR = "fnr";

    @Mock
    private LdapService ldapService;
    @Mock
    private PersonService personService;
    @Mock
    private OrganisasjonRessursEnhetService organisasjonRessursEnhetService;
    @Mock
    private OrganisasjonEnhetService organisasjonEnhetService;
    @InjectMocks
    private GeografiskTilgangService geografiskTilgangService;

    @Before
    public void setup() {
        when(ldapService.harTilgang(anyString(), any())).thenReturn(false);
        when(personService.hentGeografiskTilknytning(anyString())).thenReturn("brukersPostnummer");
        when(organisasjonEnhetService.finnNAVKontorForGT("brukersPostnummer")).thenReturn(asList("brukersEnhet", "enAnnenEnhet"));
    }

    @Test
    public void nasjonalTilgangGirTilgang() {
        when(ldapService.harTilgang(VEILEDER_UID, AdRoller.NASJONAL.rolle)).thenReturn(true);
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, BRUKER_FNR)).isTrue();
    }

    @Test
    public void utvidetTilNasjonalTilgangGirTilgang() {
        when(ldapService.harTilgang(VEILEDER_UID, AdRoller.UTVIDBAR_TIL_NASJONAL.rolle)).thenReturn(true);
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, BRUKER_FNR)).isTrue();
    }

    @Test
    public void harTilgangHvisVeilederHarTilgangTilSammeEnhetSomBruker() {
        when(organisasjonRessursEnhetService.hentVeiledersEnheter(VEILEDER_UID)).thenReturn(asList("brukersEnhet", "enHeltAnnenEnhet"));
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, BRUKER_FNR)).isTrue();
    }

    @Test
    public void harIkkeTilgangHvisVeilederIkkeHarTilgangTilSammeEnhetSomBruker() {
        when( organisasjonRessursEnhetService.hentVeiledersEnheter(VEILEDER_UID)).thenReturn(singletonList("enHeltAnnenEnhet"));
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, BRUKER_FNR)).isFalse();
    }

    @Test
    public void harTilgangHvisRegionalTilgangOgTilgangTilEnhetensFylkeskontor() {
        when(ldapService.harTilgang(VEILEDER_UID, AdRoller.REGIONAL.rolle)).thenReturn(true);
        when(organisasjonRessursEnhetService.hentVeiledersEnheter(VEILEDER_UID)).thenReturn(singletonList("fylkeskontor"));
        when(organisasjonEnhetService.hentOverordnetEnhetForNAVKontor("brukersEnhet")).thenReturn(singletonList("fylkeskontor"));
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, BRUKER_FNR)).isTrue();
    }

    @Test
    public void harIkkeTilgangHvisTilgangTilEnhetensFylkeskontorMenIkkeRegionalTilgang() {
        when(organisasjonRessursEnhetService.hentVeiledersEnheter(VEILEDER_UID)).thenReturn(singletonList("fylkeskontor"));
        assertThat(geografiskTilgangService.harGeografiskTilgang(VEILEDER_UID, BRUKER_FNR)).isFalse();
    }
}
