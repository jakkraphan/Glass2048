package com.topixoft.glass2048.app.glass;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.topixoft.glass2048.app.GameActivity;
import com.topixoft.glass2048.app.InputManager;

/**
 * Created by vitalypolonetsky on 3/25/14.
 */
public class GlassGestureDetector {

    private GestureDetector glassGestureDetector;

    private LiveCard mLiveCard;

    public GlassGestureDetector(final GameActivity gameActivity, final InputManager inputManager) {
        final Context context = gameActivity;

        glassGestureDetector = new com.google.android.glass.touchpad.GestureDetector(context);

        glassGestureDetector.setBaseListener(new com.google.android.glass.touchpad.GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.LONG_PRESS) {
                    inputManager.down();
                    return true;
                } else if (gesture == Gesture.TAP) {
                    inputManager.up();
                    return true;
                } else if (gesture == Gesture.SWIPE_RIGHT) {
                    inputManager.left();
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    inputManager.right();
                    return true;
                }
                return false;
            }
        });

        glassGestureDetector.setFingerListener(new com.google.android.glass.touchpad.GestureDetector.FingerListener() {
            @Override
            public void onFingerCountChanged(int previousCount, int currentCount) {
                if (previousCount == 1 && currentCount == 2) {
//                    startVoiceRecognition();

//                    gameActivity.finish();

//                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//                    startActivityForResult(intent, SPEECH_REQUEST);

                    gameActivity.openOptionsMenu();
                }

            }
        });

    }

    private void startVoiceRecognition(Context context) {
        SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(context);
        sr.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                for (String result : results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)) {
//                    Log.d(TAG, "result " + result);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        sr.startListening(intent);
    }

    public boolean onMotionEvent(MotionEvent event) {
        return glassGestureDetector.onMotionEvent(event);
    }
}
