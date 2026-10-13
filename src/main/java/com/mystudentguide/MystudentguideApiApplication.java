  package com.mystudentguide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principale de l'application MyStudentGuide.
 *
 * Cette classe permet de démarrer l'application Spring Boot.
 *
 * L'annotation @EnableScheduling active le système de tâches planifiées
 * de Spring.
 *
 * Elle nous permettra plus tard d'exécuter automatiquement certaines
 * méthodes, par exemple pour envoyer un rappel tous les 10 jours
 * aux étudiants concernant leurs démarches en cours.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MystudentguideApiApplication {

	/**
	 * Point d'entrée de l'application.
	 *
	 * Spring Boot démarre l'ensemble de l'application
	 * à partir de cette méthode.
	 *
	 * @param args arguments éventuellement transmis au démarrage
	 */
	public static void main(String[] args) {

		SpringApplication.run(
				MystudentguideApiApplication.class,
				args
		);
	}

}