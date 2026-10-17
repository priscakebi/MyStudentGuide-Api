package com.mystudentguide.service;

import com.mystudentguide.dto.user.UserResponse;
import com.mystudentguide.dto.user.UserUpdateRequest;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailVerificationService emailVerificationService;

    @InjectMocks private UserService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(3L);
        user.setEmail("aicha@mail.com");
        user.setPrenom("Aïcha");
        user.setNom("Diallo");
        user.setRole(RoleType.ETUDIANT);
        user.setEmailVerified(true);
        when(userRepository.findByEmail("aicha@mail.com")).thenReturn(Optional.of(user));
        lenient().when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    }

    private static UserUpdateRequest demande(String email) {
        UserUpdateRequest r = new UserUpdateRequest();
        r.setEmail(email);
        r.setPrenom(" Aïcha ");
        r.setNom("Diallo");
        return r;
    }

    @Test
    void unNouvelEmailEstAConfirmer() {
        UserResponse reponse = service.updateCurrentUser("aicha@mail.com", demande(" Aicha.Diallo@Mail.com "));

        assertThat(user.getEmail()).isEqualTo("aicha.diallo@mail.com");
        assertThat(user.getPrenom()).isEqualTo("Aïcha");
        assertThat(reponse.isEmailVerified()).isFalse();
        verify(emailVerificationService).envoyerLien(user, false);
    }

    @Test
    void garderLaMemeAdresseNeDemandePasDeConfirmation() {
        UserResponse reponse = service.updateCurrentUser("aicha@mail.com", demande("AICHA@mail.com"));

        assertThat(reponse.isEmailVerified()).isTrue();
        verify(emailVerificationService, never()).envoyerLien(any(), anyBoolean());
    }

    @Test
    void refuseUneAdresseDejaUtilisee() {
        when(userRepository.existsByEmail("autre@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.updateCurrentUser("aicha@mail.com", demande("autre@mail.com")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("déjà utilisée");
        assertThat(user.getEmail()).isEqualTo("aicha@mail.com");
    }
}
