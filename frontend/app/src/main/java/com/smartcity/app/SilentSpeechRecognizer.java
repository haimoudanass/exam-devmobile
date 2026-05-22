package com.smartcity.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Reconnaissance vocale en arrière-plan sans ouvrir l'UI Google.
 */
public class SilentSpeechRecognizer implements RecognitionListener {

    public interface Callback {
        void onListeningStarted();
        void onPartialResult(String partial);
        void onFinalResult(String text);
        void onError(String message);
        void onListeningEnded();
    }

    private final Context appContext;
    private final Callback callback;
    private SpeechRecognizer speechRecognizer;
    private boolean listening;

    public SilentSpeechRecognizer(Context context, Callback callback) {
        this.appContext = context.getApplicationContext();
        this.callback = callback;
    }

    public boolean isAvailable() {
        return SpeechRecognizer.isRecognitionAvailable(appContext);
    }

    public boolean isListening() {
        return listening;
    }

    public void startListening() {
        if (listening) {
            return;
        }
        if (!isAvailable()) {
            callback.onError(appContext.getString(R.string.voice_stt_unavailable));
            return;
        }
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext);
            speechRecognizer.setRecognitionListener(this);
        }
        listening = true;
        callback.onListeningStarted();
        speechRecognizer.startListening(buildIntent());
    }

    public void stopListening() {
        if (speechRecognizer != null && listening) {
            speechRecognizer.stopListening();
        }
    }

    public void destroy() {
        listening = false;
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    private Intent buildIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH.toLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        return intent;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {}

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onRmsChanged(float rmsdB) {}

    @Override
    public void onBufferReceived(byte[] buffer) {}

    @Override
    public void onEndOfSpeech() {
        listening = false;
        callback.onListeningEnded();
    }

    @Override
    public void onError(int error) {
        listening = false;
        callback.onListeningEnded();
        if (error == SpeechRecognizer.ERROR_CLIENT) {
            return;
        }
        if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || error == SpeechRecognizer.ERROR_NO_MATCH) {
            callback.onError(appContext.getString(R.string.voice_stt_failed));
        }
    }

    @Override
    public void onResults(Bundle results) {
        listening = false;
        callback.onListeningEnded();
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            callback.onFinalResult(matches.get(0));
        } else {
            callback.onError(appContext.getString(R.string.voice_stt_failed));
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            callback.onPartialResult(matches.get(0));
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {}
}
