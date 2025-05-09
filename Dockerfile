# Utilisation de l'image OpenJDK 17
FROM openjdk:17

# Définition du répertoire de travail
WORKDIR /app

# Copie du fichier JAR généré par Maven
COPY target/hrapi-0.0.1-SNAPSHOT.jar hrapi.jar

# Définition du port d'écoute
EXPOSE 9000

# Commande de lancement de l'application
CMD ["java", "-jar", "hrapi.jar"]
