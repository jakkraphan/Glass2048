package com.topixoft.glass2048.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AbsoluteLayout;
import android.widget.Button;

import com.topixoft.glass2048.app.glass.GlassGestureDetector;

import java.util.List;


public class GameActivity extends Activity {

    private static final String TAG = "Glass2048.GameActivity";

    public static final boolean RUNNING_ON_GLASS = "Google".equalsIgnoreCase(Build.MANUFACTURER) && Build.MODEL.startsWith("Glass");

    private static final int GRID_SIZE = 4;
    private static final int SPEECH_REQUEST = 0;

    private int gridSize = GRID_SIZE;

    private View[][] emptyCellViews = new View[gridSize][gridSize];

    private InputManager inputManager = new InputManager();

    private GestureDetector gestureDetector;

    private GlassGestureDetector glassGestureDetector;

    public GameActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_game);

        addCells();

        if (RUNNING_ON_GLASS) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            glassGestureDetector = new GlassGestureDetector(this, inputManager);

            findViewById(R.id.textViewGlassHelp).setVisibility(View.VISIBLE);
        } else {
            Button buttonNewGame = (Button) findViewById(R.id.buttonNewGame);
            buttonNewGame.setVisibility(View.VISIBLE);

            buttonNewGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inputManager.restart();
                }
            });

            findViewById(R.id.layoutEndGameButtons).setVisibility(View.VISIBLE);

            Button buttonTryAgain = (Button) findViewById(R.id.buttonTryAgain);
            buttonTryAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inputManager.restart();
                }
            });

            Button buttonKeepPlaying = (Button) findViewById(R.id.buttonKeepPlaying);
            buttonKeepPlaying.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inputManager.keepPlaying();
                }
            });
        }

        gestureDetector = new GestureDetector(this, new FlingGestureListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (RUNNING_ON_GLASS) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.game, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!RUNNING_ON_GLASS) {
            return false;
        }

        boolean over = findViewById(R.id.gameMessageContainer).getVisibility() == View.VISIBLE;
        boolean keepPlaying = findViewById(R.id.buttonKeepPlaying).getVisibility() == View.VISIBLE;

        menu.findItem(R.id.action_new_game).setVisible(!over);
        menu.findItem(R.id.action_try_again).setVisible(over);
        menu.findItem(R.id.action_keep_playing).setVisible(over && keepPlaying);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.action_new_game:
            case R.id.action_try_again:
                inputManager.restart();
                return true;
            case R.id.action_keep_playing:
                inputManager.keepPlaying();
                return true;
            case R.id.action_exit_game:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            for (String result : results) {
                Log.d(TAG, "result " + result);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addCells() {
        ViewGroup cellContainer = (ViewGroup) findViewById(R.id.cellContainer);
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                ViewGroup cellView = (ViewGroup) getLayoutInflater().inflate(R.layout.cell_layout, cellContainer, false);

                emptyCellViews[i][j] = cellView;
                cellContainer.addView(cellView);
            }
        }
    }

    class FlingGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float absVelocityX = Math.abs(velocityX);
            float absVelocityY = Math.abs(velocityY);
            if (Math.max(absVelocityX, absVelocityY) > 200) {
                if (absVelocityX > 2 * absVelocityY) {
                    if (velocityX > 0) {
                        inputManager.right();
                    } else {
                        inputManager.left();
                    }
                } else if (absVelocityY > 2 * absVelocityX) {
                    if (velocityY > 0) {
                        inputManager.down();
                    } else {
                        inputManager.up();
                    }
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (glassGestureDetector != null) {
            return glassGestureDetector.onMotionEvent(event);
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                inputManager.up();
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                inputManager.right();
            case KeyEvent.KEYCODE_DPAD_DOWN:
                inputManager.down();
            case KeyEvent.KEYCODE_DPAD_LEFT:
                inputManager.left();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (RUNNING_ON_GLASS && keyCode == KeyEvent.KEYCODE_BACK) {
            inputManager.down();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final View squareView = findViewById(R.id.gameGrid);
        squareView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
                ViewGroup.LayoutParams layout = squareView.getLayoutParams();
                if (GameActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    layout.width = squareView.getHeight();
                } else {
                    layout.height = squareView.getWidth();
                    layout.width = layout.height; // Not needed, but helps with the rest of the calculations here
                }
                squareView.setLayoutParams(layout);

                int borders = squareView.getPaddingLeft() + squareView.getPaddingRight();
                int innerGridSize = layout.width - borders;
                float size = innerGridSize / gridSize;

                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        emptyCellViews[i][j].setLayoutParams(new AbsoluteLayout.LayoutParams(
                                (int) size, (int) size,
                                (int) (i * size), (int) (j * size)));
                    }
                }

                squareView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                new GameManager(GRID_SIZE, inputManager, new Actuator(GameActivity.this, size), new StorageManager(GameActivity.this.getPreferences(MODE_PRIVATE)));
            }
        });
    }

}
