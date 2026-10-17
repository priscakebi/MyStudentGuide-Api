package com.mystudentguide.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.OptionalLong;

/**
 * Codes à usage unique basés sur le temps (TOTP, RFC 6238), compatibles avec
 * Google Authenticator, Microsoft Authenticator, 1Password, Bitwarden...
 *
 * - secret de 160 bits encodé en Base32 ;
 * - code à 6 chiffres, HMAC-SHA1, renouvelé toutes les 30 secondes ;
 * - un pas de décalage accepté de part et d'autre (horloge du téléphone).
 *
 * Fournit aussi les codes de secours (à usage unique, seule leur empreinte est stockée).
 */
@Service
public class TotpService {

    public static final String EMETTEUR = "MyStudentGuide";

    private static final int PERIODE_SECONDES = 30;
    private static final int CHIFFRES = 6;
    private static final int DECALAGE_TOLERE = 1;

    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    /** Sans caractères ambigus (0/O, 1/I/L) : les codes de secours se recopient à la main. */
    private static final String ALPHABET_SECOURS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    public static final int NOMBRE_CODES_SECOURS = 10;

    private final SecureRandom random = new SecureRandom();
    private final Clock clock;

    @Autowired
    public TotpService() {
        this(Clock.systemUTC());
    }

    /** Constructeur des tests : horloge contrôlée. */
    public TotpService(Clock clock) {
        this.clock = clock;
    }

    /** Nouveau secret aléatoire (20 octets) encodé en Base32. */
    public String genererSecret() {
        byte[] octets = new byte[20];
        random.nextBytes(octets);
        return base32(octets);
    }

    /** Lien otpauth:// affiché en QR code pour l'application d'authentification. */
    public String uri(String email, String secret) {
        String libelle = encoder(EMETTEUR + ":" + email);
        return "otpauth://totp/" + libelle
                + "?secret=" + secret
                + "&issuer=" + encoder(EMETTEUR)
                + "&algorithm=SHA1&digits=" + CHIFFRES + "&period=" + PERIODE_SECONDES;
    }

    /**
     * Vérifie un code à 6 chiffres.
     *
     * @param dernierPas dernier pas déjà accepté (null si aucun) : un code ne sert qu'une fois
     * @return le pas correspondant au code s'il est valide, vide sinon
     */
    public OptionalLong verifier(String secret, String code, Long dernierPas) {
        if (secret == null || code == null) {
            return OptionalLong.empty();
        }
        String chiffres = code.replaceAll("\\s", "");
        if (!chiffres.matches("\\d{" + CHIFFRES + "}")) {
            return OptionalLong.empty();
        }

        long pasActuel = clock.millis() / 1000 / PERIODE_SECONDES;
        byte[] cle = depuisBase32(secret);

        for (long pas = pasActuel - DECALAGE_TOLERE; pas <= pasActuel + DECALAGE_TOLERE; pas++) {
            boolean dejaUtilise = dernierPas != null && pas <= dernierPas;
            if (!dejaUtilise && MessageDigest.isEqual(
                    calculer(cle, pas).getBytes(StandardCharsets.US_ASCII),
                    chiffres.getBytes(StandardCharsets.US_ASCII))) {
                return OptionalLong.of(pas);
            }
        }
        return OptionalLong.empty();
    }

    /** Code attendu à l'instant présent (tests). */
    public String codeActuel(String secret) {
        return calculer(depuisBase32(secret), clock.millis() / 1000 / PERIODE_SECONDES);
    }

    // ============================================================
    // CODES DE SECOURS
    // ============================================================

    /** Dix codes de la forme « ABCD-EFGH » (affichés une seule fois). */
    public List<String> genererCodesSecours() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < NOMBRE_CODES_SECOURS; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < 8; j++) {
                if (j == 4) {
                    code.append('-');
                }
                code.append(ALPHABET_SECOURS.charAt(random.nextInt(ALPHABET_SECOURS.length())));
            }
            codes.add(code.toString());
        }
        return codes;
    }

    /** Empreinte stockée d'un code de secours (insensible aux tirets, espaces et à la casse). */
    public String empreinteCodeSecours(String code) {
        String normalise = code == null ? "" : code.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha.digest(normalise.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // ============================================================
    // CALCUL (RFC 4226 / 6238)
    // ============================================================

    private static String calculer(byte[] cle, long pas) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(cle, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(pas).array());

            int decalage = hash[hash.length - 1] & 0x0f;
            int binaire = ((hash[decalage] & 0x7f) << 24)
                    | ((hash[decalage + 1] & 0xff) << 16)
                    | ((hash[decalage + 2] & 0xff) << 8)
                    | (hash[decalage + 3] & 0xff);

            return String.format("%0" + CHIFFRES + "d", binaire % 1_000_000);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    static String base32(byte[] octets) {
        StringBuilder sortie = new StringBuilder();
        int tampon = 0;
        int bits = 0;
        for (byte octet : octets) {
            tampon = (tampon << 8) | (octet & 0xff);
            bits += 8;
            while (bits >= 5) {
                sortie.append(BASE32.charAt((tampon >> (bits - 5)) & 0x1f));
                bits -= 5;
            }
        }
        if (bits > 0) {
            sortie.append(BASE32.charAt((tampon << (5 - bits)) & 0x1f));
        }
        return sortie.toString();
    }

    static byte[] depuisBase32(String texte) {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        int tampon = 0;
        int bits = 0;
        for (char c : texte.toUpperCase(Locale.ROOT).replaceAll("[\\s=]", "").toCharArray()) {
            int valeur = BASE32.indexOf(c);
            if (valeur < 0) {
                throw new IllegalArgumentException("Secret Base32 invalide");
            }
            tampon = (tampon << 5) | valeur;
            bits += 5;
            if (bits >= 8) {
                sortie.write((tampon >> (bits - 8)) & 0xff);
                bits -= 8;
            }
        }
        return sortie.toByteArray();
    }

    private static String encoder(String texte) {
        return URLEncoder.encode(texte, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
