package no.nav.syfo.api;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.domain.AdRoller;
import no.nav.syfo.domain.Tilgang;
import no.nav.syfo.services.LdapService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static no.nav.syfo.domain.AdRoller.*;
import static no.nav.syfo.mocks.OrganisasjonRessursEnhetMock.VIGGO_VEILEDER;
import static no.nav.syfo.mocks.PersonMock.*;
import static no.nav.syfo.util.OidcTestHelper.loggInnVeilederMedOpenAM;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Komponent / blackbox test av møtebehovsfunskjonaliteten - test at endepunktet (controlleren, for enkelhets skyld)
 * gir riktig svar utifra hva web-servicene returnerer
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
public class TilgangRessursComponentTest {

    private static final boolean INNVILG = true;
    private static final boolean NEKT = false;

    @Inject
    private OIDCRequestContextHolder oidcRequestContextHolder;

    @Inject
    private LdapService ldapServiceMock; // TODO Forsøk å mocke selve ldap med f.eks spring.security.test

    @Inject
    TilgangRessurs tilgangRessurs;

    @Test
    public void tilgangTilTjenestenInnvilget() {
        mockRoller(VIGGO_VEILEDER, INNVILG, SYFO);
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);

        assertEquals(200, tilgangRessurs.tilgangTilTjenesten().getStatus());
    }

    @Test
    public void tilgangTilTjenestenNektet() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(VIGGO_VEILEDER, NEKT, SYFO);

        assertEquals(403, tilgangRessurs.tilgangTilTjenesten().getStatus());
    }

    @Test
    public void tilgangTilBrukerInnvilget() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(VIGGO_VEILEDER, INNVILG, SYFO);

        Response response = tilgangRessurs.tilgangTilBruker(BJARNE_BRUKER);
        assertTilgangOK(response);
    }

    @Test
    public void tilgangTilKode6BrukerNektet() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(VIGGO_VEILEDER, INNVILG, SYFO);

        Response response = tilgangRessurs.tilgangTilBruker(BENGT_KODE6_BRUKER);
        assertTilgangNektet(response, KODE6.name());
    }

    @Test
    public void tilgangTilKode6BrukerNektesAlltid() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(VIGGO_VEILEDER, INNVILG, SYFO, KODE6);

        Response response = tilgangRessurs.tilgangTilBruker(BENGT_KODE6_BRUKER);
        assertTilgangNektet(response, KODE6.name());
    }

    @Test
    public void tilgangTilKode7BrukerNektet() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(VIGGO_VEILEDER, INNVILG, SYFO);

        Response response = tilgangRessurs.tilgangTilBruker(BIRTE_KODE7_BRUKER);
        assertTilgangNektet(response, KODE7.name());
    }

    @Test
    public void tilgangTilKode7BrukerInnvilget() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(VIGGO_VEILEDER, INNVILG, SYFO, KODE7);

        Response response = tilgangRessurs.tilgangTilBruker(BIRTE_KODE7_BRUKER);
        assertTilgangOK(response);
    }

    @Test
    public void tilgangTilEgenAnsattBrukerNektet() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(VIGGO_VEILEDER, INNVILG, SYFO);

        Response response = tilgangRessurs.tilgangTilBruker(ERIK_EGENANSATT_BRUKER);
        assertTilgangNektet(response, EGEN_ANSATT.name());
    }

    @Test
    public void tilgangTilEgenAnsattBrukerInnvilget() {
        loggInnVeilederMedOpenAM(oidcRequestContextHolder, VIGGO_VEILEDER);
        mockRoller(VIGGO_VEILEDER, INNVILG, SYFO, EGEN_ANSATT);

        Response response = tilgangRessurs.tilgangTilBruker(ERIK_EGENANSATT_BRUKER);
        assertTilgangOK(response);
    }

    private void assertTilgangOK(Response response) {
        assertEquals(200, response.getStatus());
        Tilgang tilgang = (Tilgang) response.getEntity();
        assertTrue(tilgang.harTilgang());
    }

    private void assertTilgangNektet(Response response, String begrunnelse) {
        assertEquals(403, response.getStatus());
        Tilgang tilgang = (Tilgang) response.getEntity();
        assertFalse(tilgang.harTilgang());
        assertEquals(begrunnelse, tilgang.begrunnelse());
    }

    private void mockRoller(String veileder, boolean innvilget, AdRoller... roller) {
        reset(ldapServiceMock);

        for(AdRoller rolle : roller) {
            when(ldapServiceMock.harTilgang(veileder, rolle.rolle)).thenReturn(innvilget);
        }
    }

}