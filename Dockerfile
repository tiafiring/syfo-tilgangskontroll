FROM docker.adeo.no:5000/pus/maven as builder
ADD / /source
WORKDIR /source
RUN mvn package -DskipTests

FROM docker.adeo.no:5000/bekkci/nais-java-app
COPY --from=builder /source/target/syfo-tilgangskontroll /app