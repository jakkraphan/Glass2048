package com.topixoft.glass2048.app.speech;

import android.content.Context;
import android.util.Log;

import com.topixoft.glass2048.app.InputManager;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.FsgModel;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.Jsgf;
import edu.cmu.pocketsphinx.JsgfRule;
import edu.cmu.pocketsphinx.NGramModel;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.Assets.syncAssets;
import static edu.cmu.pocketsphinx.Decoder.defaultConfig;

/**
 * Created by vitalypolonetsky on 3/25/14.
 */
public class SphinxSpeechRecognition implements RecognitionListener {

    static {
//        System.loadLibrary("pocketsphinx_jni");
    }

    private static final String TAG = "SphinxSpeechRecognition";

    private final InputManager inputManager;

    private File assetDir;
    private SpeechRecognizer recognizer;

    public SphinxSpeechRecognition(Context context, InputManager inputManager) {
        this.inputManager = inputManager;

        try {
            assetDir = syncAssets(context);
        } catch (IOException e) {
            throw new RuntimeException("Failed to synchronize assets", e);
        }

        Config config = defaultConfig();
        config.setString("-dict", joinPath(assetDir, "models/lm/cmu07a.dic"));
        config.setString("-hmm", joinPath(assetDir, "models/hmm/en-us-semi"));
        config.setString("-rawlogdir", assetDir.getPath());
        config.setInt("-maxhmmpf", 10000);
        config.setBoolean("-fwdflat", false);
        config.setBoolean("-bestpath", false);
        config.setFloat("-kws_threshold", 1e-5);

        recognizer = new SpeechRecognizer(config);
        recognizer.addListener(this);

//        // Create keyword-activation search.
//        recognizer.setKws(KWS_SEARCH_NAME, KEYPHRASE);

        // Create grammar-based searches.
        int lw = config.getInt("-lw");
        addSearch("directions", "directions.gram", "<directions.direction>", lw);
//        addSearch("menu", "menu.gram", "<menu.item>", lw);
//        addSearch("digits", "digits.gram", "<digits.digits>", lw);
        // Create language model search.
//        String path = joinPath(assetDir, "models/lm/weather.dmp");
//        NGramModel lm = new NGramModel(config, recognizer.getLogmath(), path);
//        recognizer.setLm("forecast", lm);

        recognizer.addListener(this);

        recognizer.setSearch("directions");
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        String text = hypothesis.getHypstr();

        Log.d(TAG, "Recognition partial result: " + text);

        recognizer.stopListening();

//        restartListening();
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        String text = hypothesis.getHypstr();

        Log.d(TAG, "Recognition result: " + text);

        if (text.equals("up")) {
            inputManager.up();
        } else if (text.equals("down")) {
            inputManager.down();
        } else if (text.equals("right")) {
            inputManager.right();
        } else if (text.equals("left")) {
            inputManager.left();
        }

        recognizer.startListening();

    }

    private void addSearch(String name, String path, String ruleName, int lw) {
        File grammarParent = new File(joinPath(assetDir, "grammar"));
        Jsgf jsgf = new Jsgf(joinPath(grammarParent, path));
        JsgfRule rule = jsgf.getRule(ruleName);
        FsgModel fsg = jsgf.buildFsg(rule, recognizer.getLogmath(), lw);
        recognizer.setFsg(name, fsg);
    }

//    private void switchSearch(String searchName) {
//        recognizer.stopListening();
//        recognizer.setSearch(searchName);
//        recognizer.startListening();
//    }

    private void restartListening() {
        recognizer.stopListening();
        recognizer.startListening();
    }

    private static String joinPath(File parent, String path) {
        return new File(parent, path).getPath();
    }

    @Override
    public void onVadStateChanged(boolean b) {

    }

    public void onResume() {
        recognizer.startListening();
    }

    public void onPause() {
        recognizer.stopListening();
    }
}
