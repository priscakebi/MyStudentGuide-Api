package com.mystudentguide.controller;

import com.mystudentguide.dto.account.ChangePasswordRequest;
import com.mystudentguide.dto.account.DeleteAccountRequest;
import jakarta.validation.Valid;
import com.mystudentguide.service.AccountService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur responsable des opérations liées au compte
 * de l'utilisateur connecté.
 *
 * Ce contrôleur reçoit les requêtes HTTP provenant du frontend
 * et délègue la logique métier à AccountService.
 *
 * Il ne contient donc pas la logique de suppression elle-même.
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    /**
     * Service contenant la logique métier
     * liée à la gestion du compte.
     */
    private final AccountService accountService;

    /**
     * Constructeur du contrôleur.
     *
     * @param accountService service de gestion du compte
     */
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Supprime définitivement le compte de l'utilisateur connecté.
     *
     * Le frontend doit envoyer :
     *
     * DELETE /api/account
     *
     * avec un corps JSON contenant :
     *
     * {
     *     "deletionReason": "...",
     *     "deletionDetails": "...",
     *     "confirmation": true,
     *     "deletionPassword": "..."
     * }
     *
     * Le token JWT envoyé dans Authorization permet à Spring Security
     * d'identifier l'utilisateur actuellement connecté.
     *
     * @param request informations nécessaires à la suppression
     * @return réponse HTTP indiquant le résultat de l'opération
     */
    @DeleteMapping
    public ResponseEntity<?> deleteAccount(
            @RequestBody DeleteAccountRequest request
    ) {

        try {

            /*
             * On délègue toute la logique métier au service.
             *
             * Le service va notamment :
             * - vérifier le mot de passe ;
             * - récupérer les administrateurs ;
             * - créer les notifications ;
             * - envoyer les emails ;
             * - supprimer les notifications de l'étudiant ;
             * - supprimer le compte.
             */
            accountService.deleteCurrentAccount(request);

            /*
             * Si aucune exception n'est levée,
             * la suppression a été effectuée avec succès.
             */
            return ResponseEntity.ok(
                    new AccountResponse(
                            true,
                            "Votre compte a été supprimé avec succès."
                    )
            );

        } catch (BadCredentialsException e) {

            /*
             * Le mot de passe fourni est incorrect.
             *
             * HTTP 400 (et non 401) : côté front, un 401
             * déconnecte automatiquement l'utilisateur.
             */
            return ResponseEntity.badRequest().body(
                    new AccountResponse(
                            false,
                            "Le mot de passe est incorrect."
                    )
            );

        } catch (IllegalArgumentException e) {

            /*
             * La demande envoyée par le frontend
             * contient des données invalides ou manquantes.
             */
            return ResponseEntity.badRequest().body(
                    new AccountResponse(
                            false,
                            e.getMessage()
                    )
            );

        } catch (IllegalStateException e) {

            /*
             * Le serveur ne possède pas les informations nécessaires
             * pour effectuer correctement la suppression.
             */
            return ResponseEntity.internalServerError().body(
                    new AccountResponse(
                            false,
                            e.getMessage()
                    )
            );
        }
    }

    // ============================================================
    // CHANGER LE MOT DE PASSE
    // ============================================================

    /**
     * PUT /api/account/password
     *
     * Corps : { "currentPassword": "...", "newPassword": "..." }
     *
     * - 200 : mot de passe modifié ;
     * - 400 : mot de passe actuel incorrect ou nouveau mot de passe invalide.
     *
     * (Pas de 401 ici : côté front, un 401 déconnecte l'utilisateur.)
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        try {
            accountService.changePassword(
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(
                    new AccountResponse(
                            true,
                            "Votre mot de passe a été modifié avec succès."
                    )
            );
        } catch (BadCredentialsException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new AccountResponse(false, e.getMessage())
            );
        }
    }

    /**
     * Petite classe utilisée pour construire une réponse JSON
     * simple après le traitement de la suppression.
     *
     * Exemple de réponse :
     *
     * {
     *     "success": true,
     *     "message": "Votre compte a été supprimé avec succès."
     * }
     */
    private static class AccountResponse {

        /**
         * Indique si l'opération a réussi.
         */
        private final boolean success;

        /**
         * Message destiné au frontend.
         */
        private final String message;

        /**
         * Constructeur de la réponse.
         *
         * @param success résultat de l'opération
         * @param message message associé
         */
        public AccountResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        /**
         * Retourne le résultat de l'opération.
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Retourne le message.
         */
        public String getMessage() {
            return message;
        }
    }
}