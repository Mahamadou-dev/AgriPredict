# 1. Étape de Construction (Build Stage)
# Utilise l'image SDK de .NET pour compiler ton projet
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# Copie le fichier projet (csproj) et restaure les dépendances
# Cela utilise la mise en cache de Docker pour accélérer les builds futurs
COPY AgriPredict.API/AgriPredict.API.csproj AgriPredict.API/
RUN dotnet restore AgriPredict.API/AgriPredict.API.csproj

# Copie le reste du code source
COPY AgriPredict.API/. AgriPredict.API/

# Publie l'application (Compilation)
# Le résultat est mis dans le dossier /app/publish
WORKDIR /src/AgriPredict.API
RUN dotnet publish -c Release -o /app/publish

# ----------------------------------------------------

# 2. Étape Finale (Final Stage)
# Utilise l'image Runtime, beaucoup plus légère, pour l'exécution
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app

# Copie seulement les fichiers publiés (l'application compilée) depuis l'étape 'build'
COPY --from=build /app/publish .

# Définit la variable d'environnement pour l'URL d'écoute (port interne du conteneur)
ENV ASPNETCORE_URLS=http://+:80

# Commande pour démarrer l'application
# Remplace 'AgriPredict.API.dll' par le nom de ton fichier .dll si différent
ENTRYPOINT ["dotnet", "AgriPredict.API.dll"]