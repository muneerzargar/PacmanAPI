package org.fullstack5.pacmanapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameState {
    // this class is meant to be immutable
    private long time;
    private Position pacman;
}