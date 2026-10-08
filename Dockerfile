# ============================================================
# MyStudentGuide API
# Dockerfile - Spring Boot + Java 21
# ============================================================

# ------------------------------------------------------------
# Étape 1 : Image Java
# ------------------------------------------------------------
# Eclipse Temurin fournit une distribution OpenJDK fiable.
# Le projet MyStudentGuide utilise Java 21.
FROM eclipse-temurin:21-jre

# ------------------------------------------------------------
# Étape 2 : Répertoire de travail
# ------------------------------------------------------------
# Tous les fichiers nécessaires à l'application seront
# placés dans ce répertoire à l'intérieur du conteneur.
WORKDIR /app

# ------------------------------------------------------------
# Étape 3 : Copier le fichier JAR
# ------------------------------------------------------------
# Maven génère le fichier JAR dans le dossier target.
# Avant de construire l'image Docker, il faut donc avoir
# exécuté :
#
#     .\mvnw.cmd clean package
#
# Le caractère * permet de récupérer automatiquement
# le JAR généré.
COPY target/*.jar app.jar

# ------------------------------------------------------------
# Étape 4 : Port de l'application
# ------------------------------------------------------------
# Le port utilisé par l'application sera configuré par
# Spring Boot à partir de la variable d'environnement PORT.
#
# Render fournit automatiquement cette variable lors
# de l'exécution du service.
EXPOSE 8080

# ------------------------------------------------------------
# Étape 5 : Démarrage de l'application
# ------------------------------------------------------------
# Lance l'application Spring Boot.
ENTRYPOINT ["java", "-jar", "app.jar"]