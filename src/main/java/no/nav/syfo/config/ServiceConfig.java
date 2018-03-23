package no.nav.syfo.config;

import no.nav.syfo.services.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public DiskresjonskodeService diskresjonskodeService() {
        return new DiskresjonskodeService();
    }

    @Bean
    public EgenAnsattService egenAnsattService() {
        return new EgenAnsattService();
    }

    @Bean
    public TilgangService tilgangService() {
        return new TilgangService();
    }

    @Bean
    public GeografiskTilgangService geografiskTilgangService() {
        return new GeografiskTilgangService();
    }

    @Bean
    public OrganisasjonEnhetService organisasjonEnhetService() {
        return new OrganisasjonEnhetService();
    }

    @Bean
    public OrganisasjonRessursEnhetService organisasjonRessursEnhetService() {
        return new OrganisasjonRessursEnhetService();
    }

    @Bean
    public PersonService personService() {
        return new PersonService();
    }
}
