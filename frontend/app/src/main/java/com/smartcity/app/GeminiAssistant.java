package com.smartcity.app;

import android.content.Context;

import java.util.Locale;

/** Réponses simulées Gemini — affichage / TTS uniquement (clés API inchangées). */
public final class GeminiAssistant {

    private GeminiAssistant() {}

    public static String simulateResponse(Context context, String userText) {
        String lower = userText == null ? "" : userText.toLowerCase(Locale.FRENCH);

        if (lower.contains("bonjour") || lower.contains("salut") || lower.contains("bonsoir")) {
            return context.getString(R.string.voice_assistant_greeting);
        }
        if (lower.contains("signaler") || lower.contains("problème") || lower.contains("probleme")
                || lower.contains("rapport") || lower.contains("incident")) {
            return context.getString(R.string.voice_assistant_report);
        }
        if (lower.contains("carte") || lower.contains("map") || lower.contains("où") || lower.contains("ou ")) {
            return context.getString(R.string.voice_assistant_map);
        }
        return context.getString(R.string.voice_assistant_default);
    }
}
