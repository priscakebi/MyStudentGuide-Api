package com.mystudentguide.service;

import com.mystudentguide.entity.User;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.security.TotpService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalLong;

/**
 * Double authentification des administrateurs (application d'authentification).
 *
 * Activation en deux temps, pour ne jamais bloquer un compte :
 * 1. preparer() : un secret est créé (QR code à scanner), rien n'est encore exigé ;
 * 2. activer(code) : le premier code de l'application prouve que tout fonctionne ;
 *    dix codes de secours sont alors remis (affichés une seule fois).
 *
 * Ensuite, la connexion demande un code à 6 chiffres ou un code de secours
 * (chaque code de secours ne sert qu'une fois).
 */
@Service
public class DeuxFacteursService {

    /** État affiché dans « Mon compte ». */
    public record Etat(boolean active, int codesSecoursRestants) {
    }

    /** Secret à ajouter dans l'application (QR code ou saisie manuelle). */
    public record Preparation(String secret, String uri) {
    }

    private final UserRepository userRepository;
    private final TotpService totpService;
    private final PasswordEncoder passwordEncoder;

    public DeuxFacteursService(
            UserRepository userRepository,
            TotpService totpService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.totpService = totpService;
        this.passwordEncoder = passwordEncoder;
    }

    public Etat etat(String email) {
        User user = utilisateur(email);
        return new Etat(user.deuxFacteursActive(), empreintes(user).size());
    }

    /** Étape 1 : nouveau secret (remplace un secret préparé mais jamais confirmé). */
    @Transactional
    public Preparation preparer(String email) {
        User user = utilisateur(email);
        if (user.deuxFacteursActive()) {
            throw new IllegalArgumentException("La double authentification est déjà activée.");
        }

        String secret = totpService.genererSecret();
        user.setTotpSecret(secret);
        user.setTotpActive(false);
        user.setTotpDernierPas(null);
        user.setTotpCodesSecours(null);
        userRepository.save(user);

        return new Preparation(secret, totpService.uri(user.getEmail(), secret));
    }

    /** Étape 2 : le code de l'application confirme le secret ; renvoie les codes de secours. */
    @Transactional
    public List<String> activer(String email, String code) {
        User user = utilisateur(email);
        if (user.deuxFacteursActive()) {
            throw new IllegalArgumentException("La double authentification est déjà activée.");
        }
        if (user.getTotpSecret() == null) {
            throw new IllegalArgumentException("Commencez par scanner le QR code.");
        }

        OptionalLong pas = totpService.verifier(user.getTotpSecret(), code, null);
        if (pas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Code incorrect. Vérifiez l'heure de votre téléphone et saisissez le code affiché.");
        }

        user.setTotpActive(true);
        user.setTotpDernierPas(pas.getAsLong());
        List<String> codes = nouveauxCodesSecours(user);
        userRepository.save(user);
        return codes;
    }

    /** Remplace les codes de secours (les anciens ne marchent plus). */
    @Transactional
    public List<String> regenererCodesSecours(String email, String code) {
        User user = utilisateurActif(email);
        exigerCode(user, code);
        List<String> codes = nouveauxCodesSecours(user);
        userRepository.save(user);
        return codes;
    }

    /** Désactivation : mot de passe ET code exigés (une session laissée ouverte ne suffit pas). */
    @Transactional
    public void desactiver(String email, String motDePasse, String code) {
        User user = utilisateurActif(email);
        if (motDePasse == null || !passwordEncoder.matches(motDePasse, user.getPassword())) {
            throw new IllegalArgumentException("Le mot de passe est incorrect.");
        }
        exigerCode(user, code);
        reinitialiser(user);
        userRepository.save(user);
    }

    /**
     * Vérifie le code saisi à la connexion : code de l'application
     * ou code de secours (alors consommé).
     */
    @Transactional
    public boolean verifierCode(User user, String code) {
        if (!user.deuxFacteursActive() || code == null) {
            return false;
        }

        OptionalLong pas = totpService.verifier(user.getTotpSecret(), code, user.getTotpDernierPas());
        if (pas.isPresent()) {
            user.setTotpDernierPas(pas.getAsLong());
            userRepository.save(user);
            return true;
        }

        List<String> restantes = empreintes(user);
        if (restantes.remove(totpService.empreinteCodeSecours(code))) {
            user.setTotpCodesSecours(String.join(",", restantes));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /** Remet le compte sans double authentification (désactivation, secours de l'hébergeur). */
    public static void reinitialiser(User user) {
        user.setTotpActive(false);
        user.setTotpSecret(null);
        user.setTotpDernierPas(null);
        user.setTotpCodesSecours(null);
    }

    private void exigerCode(User user, String code) {
        if (!verifierCode(user, code)) {
            throw new IllegalArgumentException("Code incorrect.");
        }
    }

    private List<String> nouveauxCodesSecours(User user) {
        List<String> codes = totpService.genererCodesSecours();
        user.setTotpCodesSecours(String.join(",",
                codes.stream().map(totpService::empreinteCodeSecours).toList()));
        return codes;
    }

    private static List<String> empreintes(User user) {
        String valeur = user.getTotpCodesSecours();
        if (valeur == null || valeur.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(valeur.split(",")));
    }

    private User utilisateurActif(String email) {
        User user = utilisateur(email);
        if (!user.deuxFacteursActive()) {
            throw new IllegalArgumentException("La double authentification n'est pas activée.");
        }
        return user;
    }

    private User utilisateur(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }
}
