package com.mystudentguide.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TotpServiceTest {

    /** Secret des vecteurs de test de la RFC 6238 (SHA-1) : « 12345678901234567890 ». */
    private static final String SECRET_RFC = TotpService.base32(
            "12345678901234567890".getBytes(StandardCharsets.US_ASCII));

    private static TotpService a(long secondes) {
        return new TotpService(Clock.fixed(Instant.ofEpochSecond(secondes), ZoneOffset.UTC));
    }

    @Test
    void base32_allerRetour() {
        assertThat(SECRET_RFC).isEqualTo("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ");
        assertThat(new String(TotpService.depuisBase32(SECRET_RFC), StandardCharsets.US_ASCII))
                .isEqualTo("12345678901234567890");
    }

    @Test
    void vecteursDeLaRfc6238() {
        // Codes à 8 chiffres de la RFC, dont on garde les 6 derniers.
        assertThat(a(59).codeActuel(SECRET_RFC)).isEqualTo("287082");
        assertThat(a(1111111109).codeActuel(SECRET_RFC)).isEqualTo("081804");
        assertThat(a(1234567890).codeActuel(SECRET_RFC)).isEqualTo("005924");
    }

    @Test
    void accepteUnPasDeDecalage_maisPasDeux() {
        String code = a(1_000_000).codeActuel(SECRET_RFC);

        assertThat(a(1_000_000 + 30).verifier(SECRET_RFC, code, null)).isPresent();
        assertThat(a(1_000_000 - 30).verifier(SECRET_RFC, code, null)).isPresent();
        assertThat(a(1_000_000 + 90).verifier(SECRET_RFC, code, null)).isEmpty();
    }

    @Test
    void unCodeNeSertQuUneFois() {
        TotpService totp = a(1_000_000);
        String code = totp.codeActuel(SECRET_RFC);

        long pas = totp.verifier(SECRET_RFC, code, null).orElseThrow();

        assertThat(totp.verifier(SECRET_RFC, code, pas)).isEmpty();
    }

    @Test
    void refuseLesSaisiesInvalides() {
        TotpService totp = a(1_000_000);

        assertThat(totp.verifier(SECRET_RFC, "12345", null)).isEmpty();
        assertThat(totp.verifier(SECRET_RFC, "abcdef", null)).isEmpty();
        assertThat(totp.verifier(SECRET_RFC, null, null)).isEmpty();
        assertThat(totp.verifier(null, "123456", null)).isEmpty();
    }

    @Test
    void lesEspacesDansLeCodeSontToleres() {
        TotpService totp = a(1_000_000);
        String code = totp.codeActuel(SECRET_RFC);

        assertThat(totp.verifier(SECRET_RFC, code.substring(0, 3) + " " + code.substring(3), null)).isPresent();
    }

    @Test
    void secretEtLienOtpauth() {
        TotpService totp = new TotpService();
        String secret = totp.genererSecret();

        assertThat(secret).matches("[A-Z2-7]{32}");
        assertThat(totp.uri("admin@email.com", secret))
                .isEqualTo("otpauth://totp/MyStudentGuide%3Aadmin%40email.com?secret=" + secret
                        + "&issuer=MyStudentGuide&algorithm=SHA1&digits=6&period=30");
    }

    @Test
    void codesDeSecours_uniquesEtEmpreinteTolerante() {
        TotpService totp = new TotpService();
        List<String> codes = totp.genererCodesSecours();

        assertThat(codes).hasSize(10).allMatch(c -> c.matches("[A-Z2-9]{4}-[A-Z2-9]{4}"));
        assertThat(new HashSet<>(codes)).hasSize(10);

        String code = codes.get(0);
        assertThat(totp.empreinteCodeSecours(code.toLowerCase().replace("-", " ")))
                .isEqualTo(totp.empreinteCodeSecours(code));
    }
}
