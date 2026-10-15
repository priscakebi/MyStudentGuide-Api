package com.mystudentguide.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protection de la connexion contre les attaques par force brute.
 *
 * Après {@code maxEchecs} mots de passe incorrects en moins de
 * {@code fenetre} pour une même adresse e-mail, toute connexion à
 * cette adresse est refusée pendant {@code blocage} (HTTP 429).
 *
 * Choix :
 * - Par COMPTE, pas par adresse IP : sur Render, toutes les requêtes
 *   arrivent par le même proxy, bloquer une IP bloquerait tout le monde.
 * - La même règle s'applique à une adresse inexistante : la réponse ne
 *   révèle pas si un compte existe.
 * - Le blocage est vérifié AVANT le mot de passe : pendant le blocage,
 *   aucun mot de passe ne peut être testé, même le bon.
 * - Le blocage est temporaire : un attaquant peut gêner un compte
 *   quelques minutes, pas le bloquer durablement. L'étudiant peut
 *   toujours réinitialiser son mot de passe.
 *
 * Les compteurs sont en mémoire : suffisant pour une seule instance
 * de l'API (cas actuel). Avec plusieurs instances, il faudrait les
 * partager (base de données ou Redis).
 */
@Service
public class LoginAttemptService {

    private record Etat(int echecs, Instant premierEchec, Instant bloqueJusqua) {
    }

    private final Map<String, Etat> etats = new ConcurrentHashMap<>();

    private final int maxEchecs;
    private final Duration fenetre;
    private final Duration blocage;
    private final Clock clock;

    @Autowired
    public LoginAttemptService(
            @Value("${app.login.max-echecs:5}") int maxEchecs,
            @Value("${app.login.fenetre-minutes:15}") long fenetreMinutes,
            @Value("${app.login.blocage-minutes:15}") long blocageMinutes
    ) {
        this(maxEchecs, Duration.ofMinutes(fenetreMinutes), Duration.ofMinutes(blocageMinutes), Clock.systemUTC());
    }

    /** Constructeur des tests : horloge contrôlée. */
    LoginAttemptService(int maxEchecs, Duration fenetre, Duration blocage, Clock clock) {
        this.maxEchecs = maxEchecs;
        this.fenetre = fenetre;
        this.blocage = blocage;
        this.clock = clock;
    }

    /**
     * À appeler AVANT de vérifier le mot de passe.
     *
     * @throws CompteTemporairementBloqueException si le compte est bloqué
     */
    public void verifier(String email) {
        Etat etat = etats.get(cle(email));
        Instant maintenant = clock.instant();

        if (etat != null && etat.bloqueJusqua() != null && etat.bloqueJusqua().isAfter(maintenant)) {
            throw new CompteTemporairementBloqueException(
                    Duration.between(maintenant, etat.bloqueJusqua()).toSeconds());
        }
    }

    /** Mot de passe incorrect : un échec de plus, blocage au-delà du seuil. */
    public void echec(String email) {
        Instant maintenant = clock.instant();

        etats.compute(cle(email), (k, etat) -> {
            // Nouvelle série si aucune, si la fenêtre est passée ou si un blocage vient de finir.
            boolean nouvelleSerie = etat == null
                    || etat.premierEchec().plus(fenetre).isBefore(maintenant)
                    || (etat.bloqueJusqua() != null && !etat.bloqueJusqua().isAfter(maintenant));

            int echecs = nouvelleSerie ? 1 : etat.echecs() + 1;
            Instant premier = nouvelleSerie ? maintenant : etat.premierEchec();
            Instant bloque = echecs >= maxEchecs ? maintenant.plus(blocage) : null;

            return new Etat(echecs, premier, bloque);
        });
    }

    /** Connexion réussie : le compteur repart de zéro. */
    public void succes(String email) {
        etats.remove(cle(email));
    }

    /** Nettoyage régulier des entrées expirées (évite que la mémoire grossisse). */
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void nettoyer() {
        Instant maintenant = clock.instant();

        etats.entrySet().removeIf(e -> {
            Etat etat = e.getValue();
            boolean blocageFini = etat.bloqueJusqua() == null || !etat.bloqueJusqua().isAfter(maintenant);
            boolean fenetreFinie = etat.premierEchec().plus(fenetre).isBefore(maintenant);
            return blocageFini && fenetreFinie;
        });
    }

    /** Nombre d'adresses suivies (tests). */
    int taille() {
        return etats.size();
    }

    private static String cle(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
