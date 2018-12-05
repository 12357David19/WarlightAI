package helper;

import bot.BotState;
import main.Map;
import main.Region;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class contains the moves to be taken by the bot
 */
public class MovesChooser {

    private static BotState botState = null;
    private static Map mapCopyOfGame = null;
    List<PlaceArmiesMoveWithUtility> rootsChildrenPlaceArmiesMovesWithUtility = new ArrayList<>();
    List<AttackTransferMoveWithUtility> rootsChildrenAttackTransferMovesWithUtility = new ArrayList<>();
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
     * algorithm used: depth limited minimax search, preferably with alpha-beta pruning
     * @return list of moves for placing armies
     */
    public static ArrayList<PlaceArmiesMove> getMovesForPlacingArmies() {

        minimaxWithAlphaBetaPruningForPlacingArmies(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, true, instance.botState.getVisibleMap());

        int MAX = -100000;
        int best = -1;

        for (int i = 0; i < instance.rootsChildrenPlaceArmiesMovesWithUtility.size(); ++i) {
            if (MAX < instance.rootsChildrenPlaceArmiesMovesWithUtility.get(i).score) {
                MAX = instance.rootsChildrenPlaceArmiesMovesWithUtility.get(i).score;
                best = i;
            }
        }

        return instance.rootsChildrenPlaceArmiesMovesWithUtility.get(best).moves;
    }

    /**
     * Returns next moves for attacking new territory or transferring armies to a nearest friendly region
     * algorithm used: depth limited minimax search, preferably with alpha-beta pruning
     * @return list of moves for transferring or placing armies
     */
    public static ArrayList<AttackTransferMove> getMovesForAttackOrTransferArmies() {
        minimaxWithAlphaBetaPruningForAttackOrTransferArmies(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, true, instance.botState.getVisibleMap());

        int MAX = -100000;
        int best = -1;

        for (int i = 0; i < instance.rootsChildrenAttackTransferMovesWithUtility.size(); ++i) {
            if (MAX < instance.rootsChildrenAttackTransferMovesWithUtility.get(i).score) {
                MAX = instance.rootsChildrenAttackTransferMovesWithUtility.get(i).score;
                best = i;
            }
        }

        return instance.rootsChildrenAttackTransferMovesWithUtility.get(best).moves;
    }

    /**
     * Heuristic method
     * @return
     */
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

    /**
     * Get maximum possible combinations of place army moves
     * @param isMaxPlayer
     * @return
     */
    private static List<List<PlaceArmiesMove>> getAllPossiblePlaceArmiesMoves(boolean isMaxPlayer) {

        // get possible regions for any moves, which is: regions available to place new armies
        int armiesLeft = instance.botState.getStartingArmies();
        String playerName = isMaxPlayer ? instance.botState.getMyPlayerName() : instance.botState.getOpponentPlayerName();
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
                    setOfPlacesArmiesMoves.add(new PlaceArmiesMove(playerName, regions.get(index%length), armiesToPlaceThisRegion));
                    startArmyNumber -= armiesToPlaceThisRegion;
                }

                placeArmiesMoves.add(setOfPlacesArmiesMoves);
            }

        }

        return placeArmiesMoves;

    }

    /**
     * Mimic the movement of placing armies
     * @param moves
     * @param map
     * @return
     */
    private static Map doPlaceArmies(List<PlaceArmiesMove> moves, Map map) {

        for(PlaceArmiesMove move : moves) {
            int currentArmies = map.getRegion(move.getRegion().getId()).getArmies();
            currentArmies += move.getArmies();
            map.getRegion(move.getRegion().getId()).setArmies(currentArmies);
        }

        return map;

    }

    /**
     * Get maximum possible combinations of attack transfer moves
     * @param isMaxPlayer
     * @return
     */
    private static List<List<AttackTransferMove>> getAllPossibleAttackTransferMoves(boolean isMaxPlayer) {

        String myName = instance.botState.getMyPlayerName();
        String opponentName = instance.botState.getOpponentPlayerName();
        List<Region> visibleRegions = instance.botState.getVisibleMap().getRegions();
        List<Region> mainPlayerRegions = new ArrayList<>();
        Map copyOfVisibleMap = instance.botState.getVisibleMap().getMapCopy();
        int unitsOfArmiesInBlock = 2;

        String mainPlayer = isMaxPlayer ? myName : opponentName;

        for (Region r : visibleRegions) {
            if (r.ownedByPlayer(mainPlayer)) {
                mainPlayerRegions.add(r);
            }
        }

        List<List<AttackTransferMove>> attackTransferMoves = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            List<AttackTransferMove> setOfAttackTransferMoves = new ArrayList<>();
            while (true) {
                int randomIndex = (int) (Math.random() * mainPlayerRegions.size());
                Region selectedRegion = botState.getVisibleMap().getRegion(mainPlayerRegions.get(randomIndex).getId());

                if (selectedRegion.getArmies() >= unitsOfArmiesInBlock) {
                    List<Region> neighbors = selectedRegion.getNeighbors();
                    Collections.shuffle(neighbors);

                    for (Region toRegion : neighbors) {
                        int armiesToBeSent = (selectedRegion.getArmies() > unitsOfArmiesInBlock) ? unitsOfArmiesInBlock : selectedRegion.getArmies();
                        setOfAttackTransferMoves.add(new AttackTransferMove(mainPlayer, selectedRegion, toRegion, unitsOfArmiesInBlock));
                        selectedRegion.setArmies(selectedRegion.getArmies() - armiesToBeSent);
                    }
                }

                // break condition
                int totalArmies = 0;
                for (Region r : mainPlayerRegions) {
                    totalArmies += r.getArmies();
                }

                if (totalArmies <= 2) {
                    break;
                }
            }

            attackTransferMoves.add(setOfAttackTransferMoves);
        }

        return attackTransferMoves;

    }

    /**
     * Mimic the movement of attack transfer moves
     * @param moves
     * @param isMaxPlayer
     * @param map
     * @return
     */
    private static Map doAttackTransferArmies(List<AttackTransferMove> moves, boolean isMaxPlayer, Map map) {

        String myName = instance.botState.getMyPlayerName();
        String opponentName = instance.botState.getOpponentPlayerName();

        for(AttackTransferMove move : moves) {
            if (isMaxPlayer) {

                if (move.getToRegion().ownedByPlayer(myName)) {

                    int currentFromArmies = map.getRegion(move.getFromRegion().getId()).getArmies();
                    currentFromArmies -= move.getArmies();
                    map.getRegion(move.getFromRegion().getId()).setArmies(currentFromArmies);

                    int currentToArmies = map.getRegion(move.getToRegion().getId()).getArmies();
                    currentToArmies += move.getArmies();
                    map.getRegion(move.getToRegion().getId()).setArmies(currentToArmies);

                } else {

                    int myArmies = map.getRegion(move.getFromRegion().getId()).getArmies();
                    int opponentArmies = map.getRegion(move.getToRegion().getId()).getArmies();

                    int weKilled = (int) Math.ceil(myArmies * 0.6);
                    int theyKilled = (int) Math.floor(opponentArmies * 0.7);

                    if ((myArmies - theyKilled) > (opponentArmies - weKilled)) {
                        int currentFromArmies = map.getRegion(move.getFromRegion().getId()).getArmies();
                        currentFromArmies -= move.getArmies();
                        map.getRegion(move.getFromRegion().getId()).setArmies(currentFromArmies);

                        map.getRegion(move.getToRegion().getId()).setPlayerName(myName);
                        map.getRegion(move.getToRegion().getId()).setArmies(myArmies - theyKilled);
                    } else {
                        map.getRegion(move.getToRegion().getId()).setArmies(opponentArmies - weKilled);
                    }

                }

            } else {

                if (move.getToRegion().ownedByPlayer(opponentName)) {

                    int currentFromArmies = map.getRegion(move.getFromRegion().getId()).getArmies();
                    currentFromArmies -= move.getArmies();
                    map.getRegion(move.getFromRegion().getId()).setArmies(currentFromArmies);

                    int currentToArmies = map.getRegion(move.getToRegion().getId()).getArmies();
                    currentToArmies += move.getArmies();
                    map.getRegion(move.getToRegion().getId()).setArmies(currentToArmies);

                } else {

                    int opponentArmies = map.getRegion(move.getFromRegion().getId()).getArmies();
                    int myArmies = map.getRegion(move.getToRegion().getId()).getArmies();

                    int theyKilled = (int) Math.ceil(myArmies * 0.6);
                    int weKilled = (int) Math.floor(opponentArmies * 0.7);

                    if ((opponentArmies - weKilled) > (myArmies - theyKilled)) {
                        int currentFromArmies = map.getRegion(move.getFromRegion().getId()).getArmies();
                        currentFromArmies -= move.getArmies();
                        map.getRegion(move.getFromRegion().getId()).setArmies(currentFromArmies);

                        map.getRegion(move.getToRegion().getId()).setPlayerName(opponentName);
                        map.getRegion(move.getToRegion().getId()).setArmies(opponentArmies - weKilled);
                    } else {
                        instance.mapCopyOfGame.getRegion(move.getToRegion().getId()).setArmies(myArmies - theyKilled);
                    }

                }

            }
        }

        return map;

    }

    /**
     * Minimax with alpha beta pruning for move of placing armies
     * @param alpha
     * @param beta
     * @param depth
     * @param isMaxPlayer
     * @param copyOfMainMap
     * @return
     */
    private static int minimaxWithAlphaBetaPruningForPlacingArmies(int alpha, int beta, int depth, boolean isMaxPlayer, Map copyOfMainMap) {

        Map backupCopyOfMainMap = copyOfMainMap;

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
        List<List<PlaceArmiesMove>> movesAllPossible = getAllPossiblePlaceArmiesMoves(isMaxPlayer);

        // stopping criteria 2: if no region is available, terminate and return 0
        if (movesAllPossible.isEmpty())
            return 0;

        // at base level, clear all ????
        if (depth==0)
            instance.rootsChildrenPlaceArmiesMovesWithUtility.clear();

        // initializing max and min alpha-beta values, theoretically these should be infinity
        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;

        // loop for all possible moves
        for(int i = 0; i < movesAllPossible.size(); ++i) {
            ArrayList<PlaceArmiesMove> moves = (ArrayList<PlaceArmiesMove>) movesAllPossible.get(i);

            int currentScore = 0;

            if (isMaxPlayer) {
                copyOfMainMap = doPlaceArmies(moves, copyOfMainMap);
                currentScore = minimaxWithAlphaBetaPruningForPlacingArmies(alpha, beta, depth+1, false, copyOfMainMap);
                maxValue = Math.max(maxValue, currentScore);

                //Set alpha
                alpha = Math.max(currentScore, alpha);

                if(depth == 0)
                    instance.rootsChildrenPlaceArmiesMovesWithUtility.add(new PlaceArmiesMoveWithUtility(currentScore, moves));

            } else {
                copyOfMainMap = doPlaceArmies(moves, copyOfMainMap);
                currentScore = minimaxWithAlphaBetaPruningForPlacingArmies(alpha, beta, depth+1, true, copyOfMainMap);
                minValue = Math.min(minValue, currentScore);

                //Set beta
                beta = Math.min(currentScore, beta);
            }

            //reset board
            copyOfMainMap = backupCopyOfMainMap;

            // If a pruning has been done, then we don't evaluate the rest of the sibling states
            if ((currentScore == Integer.MAX_VALUE) || (currentScore == Integer.MIN_VALUE))
                break;
        }

        // stopping criteria 3: if it's
        return isMaxPlayer ? maxValue : minValue;
    }

    /**
     * Minimax with alpha beta pruning for move of attack or transfer armies
     * @param alpha
     * @param beta
     * @param depth
     * @param isMaxPlayer
     * @param copyOfMainMap
     * @return
     */
    private static int minimaxWithAlphaBetaPruningForAttackOrTransferArmies(int alpha, int beta, int depth, boolean isMaxPlayer, Map copyOfMainMap) {

        Map backupCopyOfMainMap = copyOfMainMap;

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
        List<List<AttackTransferMove>> movesAllPossible = getAllPossibleAttackTransferMoves(isMaxPlayer);

        // stopping criteria 2: if no region is available, terminate and return 0
        if (movesAllPossible.isEmpty())
            return 0;

        // at base level, clear all ????
        if (depth==0)
            instance.rootsChildrenPlaceArmiesMovesWithUtility.clear();

        // initializing max and min alpha-beta values, theoretically these should be infinity
        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;

        // loop for all possible moves
        for(int i = 0; i < movesAllPossible.size(); ++i) {
            ArrayList<AttackTransferMove> moves = (ArrayList<AttackTransferMove>) movesAllPossible.get(i);

            int currentScore = 0;

            if (isMaxPlayer) {
                copyOfMainMap = doAttackTransferArmies(moves, isMaxPlayer, copyOfMainMap);
                currentScore = minimaxWithAlphaBetaPruningForPlacingArmies(alpha, beta, depth+1, false, copyOfMainMap);
                maxValue = Math.max(maxValue, currentScore);

                //Set alpha
                alpha = Math.max(currentScore, alpha);

                if(depth == 0)
                    instance.rootsChildrenAttackTransferMovesWithUtility.add(new AttackTransferMoveWithUtility(currentScore, moves));

            } else {
                copyOfMainMap = doAttackTransferArmies(moves, isMaxPlayer, copyOfMainMap);
                currentScore = minimaxWithAlphaBetaPruningForPlacingArmies(alpha, beta, depth+1, true, copyOfMainMap);
                minValue = Math.min(minValue, currentScore);

                //Set beta
                beta = Math.min(currentScore, beta);
            }

            //reset board
            copyOfMainMap = backupCopyOfMainMap;

            // If a pruning has been done, then we don't evaluate the rest of the sibling states
            if ((currentScore == Integer.MAX_VALUE) || (currentScore == Integer.MIN_VALUE))
                break;
        }

        // stopping criteria 3: if it's
        return isMaxPlayer ? maxValue : minValue;
    }

    /**
     * Generates combinations of a list, based on nCr
     * @param list
     * @param n
     * @param <T>
     * @return
     */
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
