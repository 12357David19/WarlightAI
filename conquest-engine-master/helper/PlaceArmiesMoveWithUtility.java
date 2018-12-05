package helper;

import move.PlaceArmiesMove;

import java.util.ArrayList;
import java.util.List;

public class PlaceArmiesMoveWithUtility {

    int score;
    ArrayList<PlaceArmiesMove> moves;

    PlaceArmiesMoveWithUtility(int score, ArrayList<PlaceArmiesMove> moves) {
        this.score = score;
        this.moves = moves;
    }
}
