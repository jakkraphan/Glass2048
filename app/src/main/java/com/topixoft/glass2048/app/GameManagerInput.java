package com.topixoft.glass2048.app;

/**
 * Created by vitalypolonetsky on 3/24/14.
 */
public interface GameManagerInput {

    void move(GameManager.Direction direction);

    void restart();

    void keepPlaying();

}
