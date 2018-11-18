package helper;

import bot.BotState;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

import java.util.ArrayList;

/**
 * This class contains the moves to be taken by the bot
 */
public class MovesChooser {

    /**
     * Returns next moves for placing armies based on current game state
     * @param state
     * @return list of moves for placing armies
     */
    public static ArrayList<PlaceArmiesMove> getMovesForPlacingArmies(BotState state) {
        // TODO: implement this method using depth limited minimax search, preferably with alpha-beta pruning
        return new ArrayList<>();
    }

    /**
     * Returns next moves for attacking new territory or transferring armies to a nearest friendly region
     * @param state
     * @return list of moves for transfering or placing armies
     */
    public static ArrayList<AttackTransferMove> getMovesForAttackOrTransferArmies(BotState state) {
        // TODO: implement this method using depth limited minimax search, preferably with alpha-beta pruning
        return new ArrayList<>();
    }

}
