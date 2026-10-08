# ============================================================
# MyStudentGuide API
# Construction multi-stage avec Maven et Java 21
# ============================================================

# ------------------------------------------------------------
# ÉTAPE 1 : Compiler l'application Spring Boot
# ------------------------------------------------------------
# Cette image contient le JDK nécessaire pour compiler
# le code Java et générer le fichier JAR avec Maven.
FROM eclipse-temurin:21-jdk AS build

# Définit le dossier de travail de la phase de compilation.
WORKDIR /app

# Copie les fichiers Maven et le code source du projet.
# Le dossier .mvn contient la configuration du Maven Wrapper.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

# Rend le script Maven Wrapper exécutable dans Linux.
RUN chmod +x mvnw

# Compile le projet et génère le JAR sans exécuter les tests.
# Les tests pourront être lancés séparément avant le déploiement.
RUN ./mvnw -B clean package -DskipTests

# ------------------------------------------------------------
# ÉTAPE 2 : Préparer l'image d'exécution
# ------------------------------------------------------------
# Une image JRE plus légère suffit pour exécuter le JAR.
FROM eclipse-temurin:21-jre

# Définit le dossier de travail de l'application.
WORKDIR /app

# Récupère le JAR généré pendant la première étape.
# target/ existe dans l'étape build, même s'il est ignoré par Git.
COPY --from=build /app/target/*.jar app.jar

# Documente le port utilisé par l'application Spring Boot.
# Render transmettra le port via la variable d'environnement PORT.
EXPOSE 8080

# Lance l'application Spring Boot.
ENTRYPOINT ["java", "-jar", "app.jar"]