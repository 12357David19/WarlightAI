package helper;

import move.PlaceArmiesMove;

import java.util.List;

public class PlaceArmiesMoveWithUtility {

    int score;
    List<PlaceArmiesMove> moves;

    PlaceArmiesMoveWithUtility(int score, List<PlaceArmiesMove> moves) {
        this.score = score;
        this.moves = moves;
    }
}
