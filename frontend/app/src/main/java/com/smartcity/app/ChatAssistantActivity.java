package com.smartcity.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

public class ChatAssistantActivity extends AppCompatActivity {

    private RecyclerView chatRecycler;
    private EditText chatInput;
    private ImageButton btnMic;
    private TextView listeningHint;
    private ChatMessageAdapter chatAdapter;

    private TextToSpeech textToSpeech;
    private boolean ttsReady;
    private SilentSpeechRecognizer silentSpeechRecognizer;
    private boolean micListening;

    private final ActivityResultLauncher<String> audioPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (Boolean.TRUE.equals(granted)) {
                    startSilentListening();
                } else {
                    Toast.makeText(this, R.string.voice_permission_denied, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_assistant);

        MaterialToolbar toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        chatRecycler = findViewById(R.id.chatRecycler);
        chatInput = findViewById(R.id.chatInput);
        btnMic = findViewById(R.id.btnChatMic);
        listeningHint = findViewById(R.id.chatListeningHint);
        ImageButton btnSend = findViewById(R.id.btnChatSend);

        chatAdapter = new ChatMessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecycler.setLayoutManager(layoutManager);
        chatRecycler.setAdapter(chatAdapter);

        chatAdapter.addMessage(new ChatMessage(ChatMessage.TYPE_AI, getString(R.string.chat_welcome)));

        btnSend.setOnClickListener(v -> sendTypedMessage());
        chatInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendTypedMessage();
                return true;
            }
            return false;
        });

        btnMic.setOnClickListener(v -> onMicClicked());

        initTextToSpeech();
        initSilentSpeechRecognizer();
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int langResult = textToSpeech.setLanguage(Locale.FRENCH);
                ttsReady = langResult != TextToSpeech.LANG_MISSING_DATA
                        && langResult != TextToSpeech.LANG_NOT_SUPPORTED;
                if (!ttsReady) {
                    int fallback = textToSpeech.setLanguage(Locale.getDefault());
                    ttsReady = fallback != TextToSpeech.LANG_MISSING_DATA
                            && fallback != TextToSpeech.LANG_NOT_SUPPORTED;
                }
            } else {
                ttsReady = false;
            }
        });
    }

    private void initSilentSpeechRecognizer() {
        silentSpeechRecognizer = new SilentSpeechRecognizer(this, new SilentSpeechRecognizer.Callback() {
            @Override
            public void onListeningStarted() {
                runOnUiThread(() -> setListeningUi(true));
            }

            @Override
            public void onPartialResult(String partial) {
                runOnUiThread(() -> chatInput.setText(partial));
            }

            @Override
            public void onFinalResult(String text) {
                runOnUiThread(() -> {
                    chatInput.setText("");
                    setListeningUi(false);
                    if (text != null && !text.trim().isEmpty()) {
                        handleUserMessage(text.trim());
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setListeningUi(false);
                    Toast.makeText(ChatAssistantActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onListeningEnded() {
                runOnUiThread(() -> setListeningUi(false));
            }
        });
    }

    private void onMicClicked() {
        if (micListening) {
            silentSpeechRecognizer.stopListening();
            setListeningUi(false);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            return;
        }
        startSilentListening();
    }

    private void startSilentListening() {
        if (!silentSpeechRecognizer.isAvailable()) {
            Toast.makeText(this, R.string.voice_stt_unavailable, Toast.LENGTH_LONG).show();
            return;
        }
        silentSpeechRecognizer.startListening();
    }

    private void setListeningUi(boolean listening) {
        micListening = listening;
        listeningHint.setVisibility(listening ? TextView.VISIBLE : TextView.GONE);
        listeningHint.setText(R.string.chat_listening);
        int bg = listening ? R.color.category_safety : R.color.accent;
        btnMic.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, bg)));
    }

    private void sendTypedMessage() {
        String text = chatInput.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        chatInput.setText("");
        handleUserMessage(text);
    }

    private void handleUserMessage(String userText) {
        chatAdapter.addMessage(new ChatMessage(ChatMessage.TYPE_USER, userText));
        scrollToBottom();

        String aiText = GeminiAssistant.simulateResponse(this, userText);
        chatAdapter.addMessage(new ChatMessage(ChatMessage.TYPE_AI, aiText));
        scrollToBottom();
        speakAiResponse(aiText);
    }

    private void scrollToBottom() {
        int last = chatAdapter.getItemCount() - 1;
        if (last >= 0) {
            chatRecycler.smoothScrollToPosition(last);
        }
    }

    private void speakAiResponse(String text) {
        if (!ttsReady || textToSpeech == null) {
            return;
        }
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "chat_ai_tts");
    }

    @Override
    protected void onDestroy() {
        if (silentSpeechRecognizer != null) {
            silentSpeechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
