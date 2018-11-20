package bot;

import java.util.ArrayList;

import helper.MovesChooser;
import helper.StartingRegionPicker;
import main.Region;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public class BotStarterImproved implements Bot {

    @Override
    /**
     * A method used at the start of the game to decide which player start with what Regions. 6 Regions are required to be returned.
     * @return : a list of m (m=6) Regions starting with the most preferred Region and ending with the least preferred Region to start with
     */
    public ArrayList<Region> getPreferredStartingRegions(BotState state, Long timeOut) {
        ArrayList<Region> preferredStartingRegions = StartingRegionPicker.getPreferredStartingRegions(state);
        return preferredStartingRegions;
    }

    @Override
    /**
     * This method is called for at first part of each round.
     * @return The list of PlaceArmiesMoves for one round
     */
    public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) {
        MovesChooser movesChooser = MovesChooser.getInstance(state);
        ArrayList<PlaceArmiesMove> nextMovesForPlacingArmies = movesChooser.getMovesForPlacingArmies();
        MovesChooser.clear();
        return nextMovesForPlacingArmies;
    }

    @Override
    /**
     * This method is called for at the second part of each round.
     * @return The list of PlaceArmiesMoves for one round
     */
    public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) {
        MovesChooser movesChooser = MovesChooser.getInstance(state);
        ArrayList<AttackTransferMove> nextMovesForAttackOrTransfer = movesChooser.getMovesForAttackOrTransferArmies();
        MovesChooser.clear();
        return nextMovesForAttackOrTransfer;
    }

    public static void main(String[] args) {
        BotParser parser = new BotParser(new BotStarter());
        parser.run();
    }

}
