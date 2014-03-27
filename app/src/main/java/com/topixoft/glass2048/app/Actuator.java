package com.topixoft.glass2048.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vitalypolonetsky on 3/24/14.
 */
public class Actuator {

    private final Context context;
    private Resources resources;
    private final ViewGroup tileContainer;
    private final TextView textViewScore;
    private final TextView textViewBestScore;
    private final float cellSize;

    private Map<Integer, Float> textSizes = new HashMap<Integer, Float>();
    private Map<Integer, Integer> textColors = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> backgroundColors = new HashMap<Integer, Integer>();

    public Actuator(GameActivity gameActivity, float cellSize) {
        context = (Context) gameActivity;
        resources = context.getResources();

        populateStyles();

        tileContainer = (ViewGroup) gameActivity.findViewById(R.id.tileContainer);

        textViewScore = (TextView) gameActivity.findViewById(R.id.textViewScore);
        textViewBestScore = (TextView) gameActivity.findViewById(R.id.textViewBestScore);

        this.cellSize = cellSize;
    }

    private void populateStyles() {
        float fontSize = resources.getDimension(resources.getIdentifier("tile_fontSize", "dimen", context.getPackageName()));
        int textColor = resources.getColor(resources.getIdentifier("tile_color", "color", context.getPackageName()));
        int backgroundColor = resources.getColor(resources.getIdentifier("tile_backgroundColor", "color", context.getPackageName()));

        for (int i = 1; i <= 2048; i += i) {
            int resId;

            String tile = (i == 1) ? "super" : Integer.toString(i);

            resId = resources.getIdentifier("tile_" + tile + "_fontSize", "dimen", context.getPackageName());
            textSizes.put(i, resId != 0 ? resources.getDimension(resId) : fontSize);

            resId = resources.getIdentifier("tile_" + tile + "_color", "color", context.getPackageName());
            textColors.put(i, resId != 0 ? resources.getColor(resId) : textColor);

            resId = resources.getIdentifier("tile_" + tile + "_backgroundColor", "color", context.getPackageName());
            backgroundColors.put(i, resId != 0 ? resources.getColor(resId) : backgroundColor);
        }
    }

    public void actuate(Grid grid, int score, boolean over, boolean won,
                        int bestScore, boolean terminated) {
        this.clearContainer(this.tileContainer);

        for (Tile[] column : grid.cells) {
            for (Tile cell : column) {
                if (cell != null) {
                    this.addTile(cell);
                }
            }
        }

        this.updateScore(score);
        this.updateBestScore(bestScore);

        if (terminated) {
            if (over) {
                this.message(false); // You lose
            } else if (won) {
                this.message(true); // You win!
            }
        }
    }

    public void continueGame() {

    }

    private void clearContainer(ViewGroup tileContainer) {
        tileContainer.removeAllViews();
    }

    private void addTile(Tile tile) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup tileView = (ViewGroup) inflater.inflate(R.layout.tile_layout, tileContainer, false);
//        tileView.setBackgroundResource(R.drawable.shadow);

        tileView.setLayoutParams(new AbsoluteLayout.LayoutParams(
                (int) cellSize, (int) cellSize,
                (int) (tile.getX() * cellSize), (int) (tile.getY() * cellSize)));

        TextView textView = (TextView) tileView.findViewById(R.id.tile);

        GradientDrawable shapeDrawable = (GradientDrawable) resources.getDrawable(R.drawable.tile_shape);

        int resValue = tile.getValue() > 2048 ? 1 : tile.getValue();

        float textSize = textSizes.get(resValue);
        int textColor = textColors.get(resValue);
        int backgroundColor = backgroundColors.get(resValue);

        textView.setTextSize(textSize);
        textView.setTextColor(textColor);
        shapeDrawable.setColor(backgroundColor);
        shapeDrawable.setStroke(resources.getDimensionPixelSize(R.dimen.cell_stroke_width), backgroundColor);

        textView.setBackgroundDrawable(shapeDrawable);

        textView.setText(Integer.toString(tile.getValue()));



        int animDurationCoef = 1;
        Interpolator popInterpolator = new OvershootInterpolator();
        Interpolator moveInterpolator = new AccelerateDecelerateInterpolator();

        if (tile.getPreviousPosition() != null) {
            // animate from previous position
            Cell previousCell = tile.getPreviousPosition();
            TranslateAnimation anim = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, (int) (previousCell.x - tile.getX()),
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, (int) (previousCell.y - tile.getY()),
                    Animation.RELATIVE_TO_SELF, 0
                    );
            anim.setInterpolator(moveInterpolator);
            anim.setDuration(100 * animDurationCoef);

            tileView.startAnimation(anim);

        } else if (tile.getMergedFrom() != null) {
            // animate merge
            AnimationSet anim = new AnimationSet(true);
            anim.setInterpolator(popInterpolator);
            anim.setDuration(200 * animDurationCoef);

            ScaleAnimation scale = new ScaleAnimation(0.5f, 1f, 0.5f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.addAnimation(scale);

            AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
            anim.addAnimation(alpha);

            tileView.startAnimation(anim);

            for (Tile mergedFromTile : tile.getMergedFrom()) {
                addTile(mergedFromTile);
            }

            tileView.bringToFront();
        } else {
            // animate new
            AnimationSet anim = new AnimationSet(true);
            anim.setInterpolator(popInterpolator);
            anim.setDuration(200 * animDurationCoef);

            ScaleAnimation scale = new ScaleAnimation(0.5f, 1f, 0.5f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.addAnimation(scale);

            AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
            anim.addAnimation(alpha);

            tileView.startAnimation(anim);
        }

        tileContainer.addView(tileView);
    }

    private void updateScore(int score) {
        textViewScore.setText(Integer.toString(score));
    }

    private void updateBestScore(int bestScore) {
        textViewBestScore.setText(Integer.toString(bestScore));
    }

    private void message(boolean won) {
        String type    = won ? "game-won" : "game-over";
        String message = won ? "You win!" : "Game over!";

//        this.messageContainer.classList.add(type);
//        this.messageContainer.getElementsByTagName("p")[0].textContent = message;
    }

    private void clearMessage() {
//        // IE only takes one value to remove at a time.
//        this.messageContainer.classList.remove("game-won");
//        this.messageContainer.classList.remove("game-over");
    }
}
