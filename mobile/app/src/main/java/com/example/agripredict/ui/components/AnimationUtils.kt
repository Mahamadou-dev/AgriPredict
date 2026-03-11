package com.example.agripredict.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*

/**
 * Utilitaires d'animation partagés pour toute l'application.
 * Fournit des transitions cohérentes entre les écrans et composants.
 */
object AnimationUtils {

    // === Transitions de navigation ===

    /** Entrée par la droite (navigation forward) */
    fun slideInFromRight(): EnterTransition =
        slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300))

    /** Sortie vers la gauche (navigation forward) */
    fun slideOutToLeft(): ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth / 3 },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(200))

    /** Entrée par la gauche (navigation back) */
    fun slideInFromLeft(): EnterTransition =
        slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth / 3 },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300))

    /** Sortie vers la droite (navigation back) */
    fun slideOutToRight(): ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(200))

    /** Entrée par le bas (écrans modaux) */
    fun slideInFromBottom(): EnterTransition =
        slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight / 4 },
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300))

    /** Sortie vers le bas (écrans modaux) */
    fun slideOutToBottom(): ExitTransition =
        slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight / 4 },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(200))

    // === Transitions de contenu ===

    /** Fade simple pour contenu interne */
    fun fadeInContent(): EnterTransition =
        fadeIn(animationSpec = tween(400, easing = LinearOutSlowInEasing))

    fun fadeOutContent(): ExitTransition =
        fadeOut(animationSpec = tween(250))

    /** Expansion verticale douce */
    fun expandVerticallySmooth(): EnterTransition =
        expandVertically(animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                fadeIn(animationSpec = tween(300, delayMillis = 50))

    fun shrinkVerticallySmooth(): ExitTransition =
        shrinkVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                fadeOut(animationSpec = tween(200))
}

