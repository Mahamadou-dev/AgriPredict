# ============================================
# Dockerfile pour AgriPredict Backend
# ============================================
# Image Python officielle légère
FROM python:3.12-slim

# Variables d'environnement
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Répertoire de travail
WORKDIR /app

# Installation des dépendances système nécessaires pour PostgreSQL
RUN apt-get update && apt-get install -y \
    libpq-dev \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Copie des fichiers de dépendances
COPY requirements.txt .

# Installation des dépendances Python
RUN pip install --no-cache-dir -r requirements.txt

# Copie du code source
COPY . .

# Exposition du port
EXPOSE 8000

# Commande de démarrage
# Le reload est désactivé en production
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
