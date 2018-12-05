package helper;

import bot.BotState;
import main.Map;
import main.Region;
import move.AttackTransferMove;
import move.MoveResult;
import move.PlaceArmiesMove;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class contains the moves to be taken by the bot
 */
public class MovesChooser {

    private static BotState botState = null;
    private static Map mapCopyOfGame = null;
    private static Map mapBackup = null;
    List<PlaceArmiesMoveWithUtility> rootsChildrenWithUtility = new ArrayList<>();
    private static ArrayList<PlaceArmiesMove> movesForPlacingArmies = null;
    private static ArrayList<AttackTransferMove> movesForAttackOrTransferArmies = null;
    private static MovesChooser instance = null;

    /**
     * Constructor for getting Singleton class
     * @return
     */
    public static MovesChooser getInstance(BotState sentBotState) {
        if (instance == null) {
            instance = new MovesChooser();
            instance.botState = sentBotState;
            instance.mapCopyOfGame = instance.botState.getVisibleMap().getMapCopy();
            instance.mapBackup = instance.botState.getVisibleMap().getMapCopy();
        }

        return instance;
    }

    /**
     * Clears singleton object
     */
    public static void clear() {
        instance.botState = null;
        instance = null;
    }

    /**
     * Returns next moves for placing armies based on current game state
     * @return list of moves for placing armies
     */
    // TODO: implement this method using depth limited minimax search, preferably with alpha-beta pruning
    public static ArrayList<PlaceArmiesMove> getMovesForPlacingArmies() {
        minimaxWithAlphaBetaPruningForPlacingArmies(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, true);
        return instance.movesForPlacingArmies;
    }

    /**
     * Returns next moves for attacking new territory or transferring armies to a nearest friendly region
     * @return list of moves for transferring or placing armies
     */
    // TODO: implement this method using depth limited minimax search, preferably with alpha-beta pruning
    public static ArrayList<AttackTransferMove> getMovesForAttackOrTransferArmies() {
        // minimaxWithAlphaBetaPruningForAttackOrTransferArmies(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, true);
        // return instance.movesForAttackOrTransferArmies;

        ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
        String myName = instance.botState.getMyPlayerName();
        int armies = 5;

        for(Region fromRegion : instance.botState.getVisibleMap().getRegions())
        {
            if(fromRegion.ownedByPlayer(myName)) //do an attack
            {
                ArrayList<Region> possibleToRegions = new ArrayList<Region>();
                possibleToRegions.addAll(fromRegion.getNeighbors());

                while(!possibleToRegions.isEmpty())
                {
                    double rand = Math.random();
                    int r = (int) (rand*possibleToRegions.size());
                    Region toRegion = possibleToRegions.get(r);

                    if(!toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 6) //do an attack
                    {
                        attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
                        break;
                    }
                    else if(toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 1) //do a transfer
                    {
                        attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
                        break;
                    }
                    else
                        possibleToRegions.remove(toRegion);
                }
            }
        }

        return attackTransferMoves;

    }

    // extracts heuristics
    private static int evaluateBoard() {

        int utility = 0;

        LinkedList<Region> visibleRegions = instance.botState.getVisibleMap().getRegions();
        for (Region r : visibleRegions) {
            if (r.ownedByPlayer(instance.botState.getMyPlayerName())) {
                utility += 50;
                utility += r.getArmies();
            } else if (r.ownedByPlayer(instance.botState.getOpponentPlayerName())) {
                utility -= 50;
                utility -= r.getArmies();
            }

        }

        return utility;
    }

    private static void addArmies(List<PlaceArmiesMove> moves) {

        String myName = instance.botState.getMyPlayerName();
        String opponentName = instance.botState.getOpponentPlayerName();
        instance.mapBackup = instance.mapCopyOfGame;

        for(PlaceArmiesMove move : moves) {
            if (move.getPlayerName().equals(myName)) {
                int currentArmies = instance.mapCopyOfGame.getRegion(move.getRegion().getId()).getArmies();
                currentArmies += move.getArmies();
                instance.mapCopyOfGame.getRegion(move.getRegion().getId()).setArmies(currentArmies);
            }
        }

    }

    private static void removeArmies(List<PlaceArmiesMove> moves) {

        instance.mapCopyOfGame = instance.mapBackup;

    }

    private static List<List<PlaceArmiesMove>> getAvailableStates() {

        // get possible regions for any moves, which is: regions available to place new armies
        int armiesLeft = instance.botState.getStartingArmies();
        String myName = instance.botState.getMyPlayerName();
        int unitsOfArmiesInBlock = 2;
        int numberOfBlocks = (int) Math.ceil(armiesLeft / unitsOfArmiesInBlock);
        List<Region> visibleRegions = instance.botState.getVisibleMap().getRegions();
        List<List<Region>> combinationsOfRegions = combinations(visibleRegions, numberOfBlocks);
        List<List<PlaceArmiesMove>> placeArmiesMoves = new ArrayList<>();

        for (List<Region> regions: combinationsOfRegions) {
            int startArmyNumber = armiesLeft;
            List<PlaceArmiesMove> setOfPlacesArmiesMoves = new ArrayList<>();
            int length = regions.size();

            for (int i = 0; i < length; i++) {
                int index = i;
                for (int j = 0; j < length; j++) {
                    int armiesToPlaceThisRegion = (startArmyNumber >= unitsOfArmiesInBlock) ? unitsOfArmiesInBlock : startArmyNumber;
                    setOfPlacesArmiesMoves.add(new PlaceArmiesMove(myName, regions.get(index%length), armiesToPlaceThisRegion));
                    startArmyNumber -= armiesToPlaceThisRegion;
                }

                placeArmiesMoves.add(setOfPlacesArmiesMoves);
            }

        }
        
        return placeArmiesMoves;

    }

    private static int minimaxWithAlphaBetaPruningForPlacingArmies(int alpha, int beta, int depth, boolean isMaxPlayer) {

        // if beta is greater than or equal to alpha, prune the branch
        if (beta <= alpha) {
            System.out.println("Pruning at depth = "+depth);
            return isMaxPlayer ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }

        // stopping criteria 1: if in max depth or the game is over, terminate and return score
        // not handling game over cases right now
        if(depth == Config.MINIMAX_SEARCH_DEPTH) //  || isGameOver()
            return evaluateBoard();

        // find all possible moves
        List<List<PlaceArmiesMove>> movesAllPossible = getAvailableStates();

        // stopping criteria 2: if no region is available, terminate and return 0
        if (movesAllPossible.isEmpty())
            return 0;

        // at base level, clear all ????
        if (depth==0)
            instance.rootsChildrenWithUtility.clear();

        // initializing max and min alpha-beta values, theoretically these should be infinity
        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;

        // loop for all possible moves
        for(int i = 0; i < movesAllPossible.size(); ++i) {
            List<PlaceArmiesMove> moves = movesAllPossible.get(i);

            int currentScore = 0;

            if (isMaxPlayer) {
                addArmies(moves);
                //placeAMove(moves, 1);
                currentScore = minimaxWithAlphaBetaPruningForPlacingArmies(alpha, beta, depth+1, false);
                maxValue = Math.max(maxValue, currentScore);

                //Set alpha
                alpha = Math.max(currentScore, alpha);

                if(depth == 0)
                    instance.rootsChildrenWithUtility.add(new PlaceArmiesMoveWithUtility(currentScore, moves));

            } else {
                addArmies(moves);
                //placeAMove(moves, 2);
                currentScore = minimaxWithAlphaBetaPruningForPlacingArmies(alpha, beta, depth+1, true);
                minValue = Math.min(minValue, currentScore);

                //Set beta
                beta = Math.min(currentScore, beta);
            }

            //reset board
            //board[point.x][point.y] = 0;
            removeArmies(moves);

            // If a pruning has been done, then we don't evaluate the rest of the sibling states
            if ((currentScore == Integer.MAX_VALUE) || (currentScore == Integer.MIN_VALUE))
                break;
        }

        // stopping criteria 3: if it's
        return isMaxPlayer ? maxValue : minValue;
    }

//    private static int minimaxWithAlphaBetaPruningForAttackOrTransferArmies(int alpha, int beta, int depth, boolean isMaxPlayer) {
//        return null;
//    }

    static <T> List<List<T>> combinations( List<T> list, int n ){

        List<List<T>> result;

        if( list.size() <= n ){

            result = new ArrayList<List<T>>();
            result.add( new ArrayList<T>(list) );

        }else if( n <= 0 ){

            result = new ArrayList<List<T>>();
            result.add( new ArrayList<T>() );

        }else{

            List<T> sublist = list.subList( 1, list.size() );

            result = combinations( sublist, n );

            for( List<T> alist : combinations( sublist, n-1 ) ){
                List<T> thelist = new ArrayList<T>( alist );
                thelist.add( list.get(0) );
                result.add( thelist );
            }
        }

        return result;
    }
    
}
