package com.topixoft.glass2048.app;

/**
 * Created by vitalypolonetsky on 3/24/14.
 */
public class InputManager {

    GameManagerInput gameManagerInput = new GameManagerInput() {
        @Override public void move(GameManager.Direction direction) { }
        @Override public void restart() { }
        @Override public void keepPlaying() { }
    };

    public void bind(GameManagerInput gameManagerInput) {
        this.gameManagerInput = gameManagerInput;
    }

    public void up() {
        gameManagerInput.move(GameManager.Direction.Up);
    }

    public void right() {
        gameManagerInput.move(GameManager.Direction.Right);
    }

    public void down() {
        gameManagerInput.move(GameManager.Direction.Down);
    }

    public void left() {
        gameManagerInput.move(GameManager.Direction.Left);
    }

    public void restart() {
        gameManagerInput.restart();
    }

    public void keepPlaying() {
        gameManagerInput.keepPlaying();
    }

}
