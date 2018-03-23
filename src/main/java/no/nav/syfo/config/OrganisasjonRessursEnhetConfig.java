package no.nav.syfo.config;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.syfo.mocks.OrganisasjonRessursEnhetMock;
import no.nav.tjeneste.virksomhet.organisasjon.ressurs.enhet.v1.OrganisasjonRessursEnhetV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class OrganisasjonRessursEnhetConfig {

    private static final String MOCK_KEY = "organisasjonressursenhet.withmock";
    private static final String ENDEPUNKT_URL = getProperty("VIRKSOMHET_ORGANISASJONRESSURSENHET_V1_ENDPOINTURL");
    private static final String ENDEPUNKT_NAVN = "ORGANISASJONRESSURSENHET_V1";
    private static final boolean KRITISK = true;

    @Bean
    public OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1() {
        OrganisasjonRessursEnhetV1 prod = factory().configureStsForOnBehalfOfWithJWT().build();
        OrganisasjonRessursEnhetV1 mock = new OrganisasjonRessursEnhetMock();

        return createMetricsProxyWithInstanceSwitcher(ENDEPUNKT_NAVN, prod, mock, MOCK_KEY, OrganisasjonRessursEnhetV1.class);
    }

    @Bean
    public Pingable organisasjonRessursEnhetV1Ping() {
        Pingable.Ping.PingMetadata pingMetadata = new Pingable.Ping.PingMetadata(ENDEPUNKT_URL, ENDEPUNKT_NAVN, KRITISK);
        final OrganisasjonRessursEnhetV1 organisasjonRessursEnhetV1 = factory()
                .configureStsForSystemUserInFSS()
                .build();
        return () -> {
            try {
                organisasjonRessursEnhetV1.ping();
                return lyktes(pingMetadata);
            } catch (Exception e) {
                return feilet(pingMetadata, e);
            }
        };
    }

    private CXFClient<OrganisasjonRessursEnhetV1> factory() {
        return new CXFClient<>(OrganisasjonRessursEnhetV1.class).address(ENDEPUNKT_URL);
    }
}