#!/bin/bash

# Port souhaité par défaut
PORT=8080

# Fonction pour tester si un port est utilisé
is_port_in_use() {
    lsof -i :$1 > /dev/null
}

# Vérification du port
while is_port_in_use $PORT; do
    echo "⚠️ Le port $PORT est déjà utilisé. Tentative sur le port suivant..."
    PORT=$((PORT + 1))
done

echo "🚀 Port $PORT disponible. Lancement de l'application..."

# Exécution avec le port détecté
mvn clean spring-boot:run -Dspring-boot.run.arguments="--server.port=$PORT"