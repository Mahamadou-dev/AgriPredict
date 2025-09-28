# Étape de construction
FROM node:22-alpine AS builder
WORKDIR /app

# AJOUT CRITIQUE: Définition explicite du PATH pour que les binaires locaux soient trouvés
ENV PATH /app/node_modules/.bin:$PATH

# Installation de pnpm (beaucoup plus rapide que npm)
RUN npm install -g pnpm@9.0.0

# Copie des fichiers de configuration AVEC le lock file en UNE SEULE LIGNE
# Cela inclut package.json, pnpm-lock.yaml, et tsconfig.json
COPY package.json pnpm-lock.yaml tsconfig.json ./

# Installation des dépendances avec pnpm
RUN pnpm install --frozen-lockfile

# Copie du reste du code source
COPY . .

# Construction de l'application
# Nous exécutons la commande 'next build' directement car le PATH est configuré
RUN next build --turbopack

# ----------------------------------------------------

# Étape d'exécution
FROM node:22-alpine AS runner
WORKDIR /app

# Création d'un utilisateur non-root pour la sécurité
RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

# Copie des fichiers construits (y compris les fichiers nécessaires au runtime Next.js)
COPY --from=builder /app/public ./public
# NOTE : Next.js 'standalone' inclut node_modules, donc nous n'avons pas besoin de copier /app/node_modules
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static
COPY --from=builder --chown=nextjs:nodejs /app/package.json ./package.json

USER nextjs

EXPOSE 3000

ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

CMD ["node", "server.js"]
