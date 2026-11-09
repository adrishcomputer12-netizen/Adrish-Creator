package com.adrish.creator;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/** A small starter voice assistant with on-device speech output and Android speech recognition. */
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int REQUEST_SPEECH = 1001;
    private LinearLayout messages;
    private ScrollView scrollView;
    private EditText messageInput;
    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textToSpeech = new TextToSpeech(this, this);
        buildScreen();
        addMessage("Hi! I’m Jarvis, your starter assistant. Tap the microphone or type a message. Try ‘time’, ‘date’, ‘help’, or ‘search for …’.", false);
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(8, 15, 29));
        root.setPadding(dp(18), dp(12), dp(18), dp(14));
        setContentView(root);

        TextView title = new TextView(this);
        title.setText("JARVIS");
        title.setTextColor(Color.rgb(92, 220, 255));
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(42)));

        TextView subtitle = new TextView(this);
        subtitle.setText("PERSONAL VOICE ASSISTANT");
        subtitle.setTextColor(Color.rgb(147, 168, 193));
        subtitle.setTextSize(11);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(24)));

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(-1, 0, 1f);
        scrollParams.topMargin = dp(12);
        scrollParams.bottomMargin = dp(12);
        root.addView(scrollView, scrollParams);

        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(messages, new ScrollView.LayoutParams(-1, -2));

        LinearLayout composer = new LinearLayout(this);
        composer.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(composer, new LinearLayout.LayoutParams(-1, dp(54)));

        messageInput = new EditText(this);
        messageInput.setSingleLine(true);
        messageInput.setHint("Ask Jarvis something…");
        messageInput.setHintTextColor(Color.rgb(142, 157, 179));
        messageInput.setTextColor(Color.WHITE);
        messageInput.setTextSize(15);
        messageInput.setPadding(dp(14), 0, dp(14), 0);
        messageInput.setImeOptions(EditorInfo.IME_ACTION_SEND);
        messageInput.setBackground(rounded(Color.rgb(24, 36, 57), dp(18)));
        composer.addView(messageInput, new LinearLayout.LayoutParams(0, -1, 1f));

        Button micButton = makeButton("🎙", Color.rgb(27, 105, 139));
        LinearLayout.LayoutParams micParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        micParams.leftMargin = dp(8);
        composer.addView(micButton, micParams);
        micButton.setOnClickListener(v -> startVoiceInput());

        Button sendButton = makeButton("➤", Color.rgb(21, 148, 181));
        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        sendParams.leftMargin = dp(6);
        composer.addView(sendButton, sendParams);
        sendButton.setOnClickListener(v -> sendTypedMessage());
        messageInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendTypedMessage();
                return true;
            }
            return false;
        });
    }

    private Button makeButton(String label, int color) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(Color.WHITE);
        button.setTextSize(18);
        button.setPadding(0, 0, 0, 0);
        button.setBackground(rounded(color, dp(16)));
        return button;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private void sendTypedMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;
        messageInput.setText("");
        hideKeyboard();
        handleUserMessage(text);
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Jarvis");
        try {
            startActivityForResult(intent, REQUEST_SPEECH);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SPEECH && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) handleUserMessage(results.get(0));
        }
    }

    private void handleUserMessage(String text) {
        addMessage(text, true);
        String normalized = text.toLowerCase(Locale.ROOT).trim();
        String response;

        if (normalized.matches(".*\\b(time|clock)\\b.*")) {
            response = "It is " + DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()) + ".";
        } else if (normalized.matches(".*\\b(date|day)\\b.*")) {
            response = "Today is " + DateFormat.getDateInstance(DateFormat.FULL).format(new Date()) + ".";
        } else if (normalized.contains("help") || normalized.contains("what can you do")) {
            response = "I can tell you the time and date, search the web, and open websites. Try ‘search for space news’ or ‘open YouTube’. This starter version does not connect to an AI service.";
        } else if (normalized.startsWith("search for ") || normalized.startsWith("search ")) {
            String query = normalized.startsWith("search for ") ? text.substring(11).trim() : text.substring(7).trim();
            if (query.isEmpty()) {
                response = "What would you like me to search for?";
            } else {
                openUrl("https://www.google.com/search?q=" + Uri.encode(query));
                response = "Here are web search results for “" + query + "”.";
            }
        } else if (normalized.startsWith("open ")) {
            String site = normalized.substring(5).trim().replace(" ", "");
            if (site.isEmpty()) {
                response = "Tell me which website to open.";
            } else {
                if (!site.contains(".")) site += ".com";
                if (!site.startsWith("http://") && !site.startsWith("https://")) site = "https://" + site;
                openUrl(site);
                response = "Opening " + site + ".";
            }
        } else if (normalized.contains("hello") || normalized.contains("hey") || normalized.contains("hi jarvis")) {
            response = "Hello! How can I help you?";
        } else if (normalized.contains("thank")) {
            response = "You’re welcome!";
        } else {
            response = "I’m listening, but this starter version has limited offline commands. Ask for the time or date, say ‘help’, or ask me to search the web.";
        }

        addMessage(response, false);
        speak(response);
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, "No app is available to open that link.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMessage(String text, boolean fromUser) {
        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextColor(Color.rgb(238, 244, 255));
        bubble.setTextSize(15);
        bubble.setPadding(dp(14), dp(11), dp(14), dp(11));
        bubble.setBackground(rounded(fromUser ? Color.rgb(22, 105, 142) : Color.rgb(25, 37, 58), dp(16)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.78f);
        params.gravity = fromUser ? Gravity.END : Gravity.START;
        params.topMargin = dp(5);
        params.bottomMargin = dp(5);
        messages.addView(bubble, params);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void hideKeyboard() {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) manager.hideSoftInputFromWindow(messageInput.getWindowToken(), 0);
    }

    private void speak(String text) {
        if (ttsReady && textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis-response");
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && textToSpeech != null) {
            ttsReady = textToSpeech.setLanguage(Locale.getDefault()) != TextToSpeech.LANG_MISSING_DATA
                    && textToSpeech.setLanguage(Locale.getDefault()) != TextToSpeech.LANG_NOT_SUPPORTED;
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
