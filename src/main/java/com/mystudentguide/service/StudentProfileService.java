package com.mystudentguide.service;

import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.repository.StudentProfileRepository;
import com.mystudentguide.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public StudentProfileService(
            StudentProfileRepository studentProfileRepository,
            UserRepository userRepository) {

        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
    }

    public List<StudentProfile> getAllProfiles() {
        return studentProfileRepository.findAll();
    }

    public Optional<StudentProfile> getProfileById(Long id) {
        return studentProfileRepository.findById(id);
    }

    public Optional<StudentProfile> getProfileByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId);
    }

    public Optional<StudentProfile> getProfileByEmail(String email) {
        return studentProfileRepository.findByUserEmail(email);
    }

    public Optional<StudentProfile> getProfileByUserEmail(String email) {
        return studentProfileRepository.findByUserEmail(email);
    }

    public boolean profileExistsForUser(Long userId) {
        return studentProfileRepository.existsByUserId(userId);
    }

    public StudentProfile saveProfile(
            StudentProfile studentProfile,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur non trouvé : " + email
                        )
                );

        // Association obligatoire avec l'utilisateur
        studentProfile.setUser(user);

        // Synchronisation du côté User
        user.setStudentProfile(studentProfile);

        return studentProfileRepository.save(studentProfile);
    }

    public void deleteProfile(Long id) {
        studentProfileRepository.deleteById(id);
    }
}