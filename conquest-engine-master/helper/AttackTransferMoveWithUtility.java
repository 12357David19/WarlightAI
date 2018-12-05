package helper;

import move.AttackTransferMove;

import java.util.ArrayList;
import java.util.List;

public class AttackTransferMoveWithUtility {
    int score;
    ArrayList<AttackTransferMove> moves;

    AttackTransferMoveWithUtility(int score, ArrayList<AttackTransferMove> moves) {
        this.score = score;
        this.moves = moves;
    }
}
