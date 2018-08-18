package com.davewhitesoftware.woodsywalk;

//
//  Class to represent an entire game of Woodsy Walk.  The toByteArray method produces a
//  serialized version which is what we pass to Google Play Game Services as our game state.
//  So this object should contain everything needed to play a game, by passing back and forth
//  all the pieces in their shuffled order, which piece we are on, everyone's game boards,
//  which colors of houses and people pieces have been placed, etc.
//


import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.davewhitesoftware.woodsywalk.R;

public class WoodsyGameData implements Serializable {
    static final long serialVersionUID = 1L;
    private ArrayList<String> participantIds = new ArrayList<String>();
    private String winner = "";        // when a game is completed, this is the participantId of the winner in the participant array above
    private ArrayList<Integer> remainingHouses = new ArrayList<Integer>();
    private ArrayList<Integer> remainingPersons = new ArrayList<Integer>();
    private HashMap<String, ArrayList<Integer>> piecesToPlay = new HashMap<String,ArrayList<Integer>>();   // all the remaining pieces to play for each player.
    private HashMap<String, WoodsyBoardData> boards = new HashMap<String, WoodsyBoardData>();  // all the boards for the players
    private HashMap<String, Integer> scores = new HashMap<String, Integer>();    // all the player scores
    private int[] pieceBag;     // the bag of pieces shuffled in random order, that gets assigned to each new participant.
    private int minPiecesLeft;  // when we play a piece, if our individual player piece bag has fewer than this, we update the value.  If a new player enters, they discard pieces from the front until they have this number.  This keeps all the players playing the same piece.
    private int[] personScores;     // when a person meets a house, you get this number of points, and then the # of points is decremented until it is zero.

    private transient String currentParticipant;
    private transient WoodsyBoardData currentBoard = new WoodsyBoardData();   // the current board while we are making changes
    private transient ArrayList<Integer> currentTurnPieces = new ArrayList<Integer>();
    private transient ArrayList<Integer> currentTurnPlayedPieces = new ArrayList<Integer>();
    private transient boolean movingPerson = false;     // true if the current turn has become moving a person
    private transient int personMovesLeft = 0;  // During person moves, number of moves left.
    private transient boolean turnFinished = false;
    private transient Coordinates personCoordinates = new Coordinates(0,0); // track where the person is during person move
    private transient String lastErrorMessage = "";
    private transient Context currentContext;   // used for extracting string resources

    WoodsyGameData(ArrayList<String> inputParticipantIds) {
        // Constructor: create a Woodsy game data structure based on the supplied player ID's.
        this.lastErrorMessage = "";
        this.participantIds.clear();
        this.remainingHouses.clear();
        this.remainingPersons.clear();
        this.personScores = new int[Pieces.numberOfPeople()];
        for (int i = 1; i <= Pieces.numberOfPeople(); i++) {
            this.remainingHouses.add(Pieces.createHousePiece(i));
            this.remainingPersons.add(Pieces.createPersonPiece(i));
            this.personScores[i-1] = this.maxPointsForGoal();
        }
        this.piecesToPlay.clear();
        this.boards.clear();
        this.scores.clear();
        //
        //  prepare the piece bag
        this.pieceBag = Pieces.pieces();
        this.minPiecesLeft = this.pieceBag.length;
        //
        //  now, add all the known  participants.
        //
        for(String thisParticipantId : inputParticipantIds) {
            if (thisParticipantId != null) this.addParticipantIfNeeded(thisParticipantId);
        }
    }

    // methods that return game parameters-- currently constants
    public int pointsForGold() { return 2; }
    public int pointsForSilver() { return 1; }
    public int maxPointsForGoal() { return 5; }

    // private methods
    private void addParticipantIfNeeded(String participantId) {
        // Checks to see if a participant is already in the game and, if not, adds their
        // information.
        if (this.participantIds.contains(participantId)) return;   // already exists
        this.participantIds.add(participantId);
        this.scores.put(participantId, 0);
        this.boards.put(participantId, new WoodsyBoardData());
        this.piecesToPlay.put(participantId, new ArrayList<Integer>());
        //  The board manages itself, but for Pieces to Play, we have to copy the piece bag.
        //  If other players have played pieces, we remove the ones they played from the front.
        for (int thisPiece : this.pieceBag) this.piecesToPlay.get(participantId).add(thisPiece);
        while (this.piecesToPlay.get(participantId).size() > this.minPiecesLeft)
            this.piecesToPlay.get(participantId).remove(0);

    }

    // methods for accessing data and modifying it during the game
    public WoodsyBoardData getBoard(String participantId) {
        // returns the board corresponding to a participant ID, or null if not found
        return this.boards.get(participantId);
    }

    public void beginTurn(String participantId, Context contextSource) {
        // Begin the current turn by copying the current player's board to the current board property.
        // When you begin a turn, you also link the game data to an Android context that can get string resources.
        this.currentParticipant = participantId;
        this.currentBoard.copyFrom(this.getBoard(participantId));
        this.currentContext = contextSource;
        // Also determine the current pieces to play on this turn, based on
        // availability of house/person pieces and number of players (2 player game:
        // each player plays 2 houses and 2 persons at the start)
        this.currentTurnPieces.clear();
        this.currentTurnPlayedPieces.clear();
        if (this.remainingPersons.size() > 0) {
            int numPairs = 1;
            if (this.participantIds.size() == 2) numPairs = 2;
            for (int i = 0; i < numPairs; i++) {
                if (i < this.remainingPersons.size())
                    this.currentTurnPieces.add(this.remainingPersons.get(i));
                if (i < this.remainingHouses.size())
                    this.currentTurnPieces.add(this.remainingHouses.get(i));
            }
        }
        if (this.currentTurnPieces.size() == 0) {
            // if no people or houses to place, place the next regular piece
            if (this.piecesToPlay.get(participantId).size() > 0)
                this.currentTurnPieces.add(this.piecesToPlay.get(participantId).get(0));
        }
        // Prepare various turn related transient properties.
        this.movingPerson = false;
        this.personMovesLeft = 0;
        this.turnFinished = false;
        this.personCoordinates = new Coordinates(0,0);
        this.lastErrorMessage = "";
    }

    public void rewindTurn() {
        // use this if the user rewinds their turn to the beginning.
        this.beginTurn(this.currentParticipant, this.currentContext);
    }

    public boolean piecesLeft() {
        // returns whether there are pieces left to play this turn.
        if (this.currentTurnPieces.size() == 0) this.turnFinished = true;
        if (this.turnFinished) return false;
        return true;
    }

    public int getNextPiece() {
        // during a turn, this returns the next piece to play, which can be displayed
        // in the next piece area.  Returns the End of Turn piece if none remain.
        if (!this.piecesLeft()) return Pieces.createEndOfTurnPiece();
        return this.currentTurnPieces.get(0);
    }

    public String getLastErrorMessage() {
        // returns the last error message generally produced by playPieceAt.
        return this.lastErrorMessage;
    }

    private int setFailure(String msg) {
        // Sets the last error message to msg, and returns the failure piece.
        this.lastErrorMessage = msg;
        return Pieces.createFailurePiece();
    }

    private String getString(int resId) {
        // uses the current context, set during beginTurn, to get a string resource.
        return this.currentContext.getString(resId);
    }

    private void setCurrentPiecePlayed() {
        // used inside of playPieceAt to move a piece that was played from the "to-play" array to the "played" array.
        if (this.currentTurnPieces.size()==0) return;
        this.currentTurnPlayedPieces.add(this.currentTurnPieces.get(0));
        this.currentTurnPieces.remove(0);
    }

    private void incrementScore(int x) {
        // Increments the score of the current player.
        int newScore = this.scores.get(this.currentParticipant) + x;
        this.scores.put(this.currentParticipant, newScore);
    }

    public int playPieceAt(int p, int x, int y) {
        //  Plays the valid piece P at (x,y) on the board.  Returns the Success piece on regular success
        //  (with the board updated), the Failure piece if the move is invalid, or, if a person has
        //  begun moving, it returns a pure Person piece to indicate which person is moving.
        //  If you get a failure piece, you can get an error message using getLastErrorMessage.
        Coordinates c = new Coordinates(x,y);
        // check if there are no moves left
        if (this.movingPerson && this.personMovesLeft <= 0) return this.setFailure(getString(R.string.all_moves_finished));
        if (!this.piecesLeft()) return this.setFailure(getString(R.string.turn_finished));
        // check for valid coordinates, retrieve the current piece on the board.
        if (!this.currentBoard.isValidCoords(c)) return this.setFailure(getString(R.string.invalid_coords));
        int currentPiece = this.currentBoard.getCell(c);
        // check game rules and place piece based on which kind it is.
        if (Pieces.isPerson(p) && this.movingPerson) {
            // Playing a single step in moving a person.  First, see if there is a path to move the person.
            // (The situation of having no moves left was already handled up top.)
            int sourcePiece = this.currentBoard.getCell(this.personCoordinates);
            int thisPersonPiece = Pieces.getPersonPieceFrom(sourcePiece);
            int thisPersonNumber = Pieces.personNumber(thisPersonPiece);
            int goalPiece = this.currentBoard.createGoalPiece(thisPersonNumber,0,c);
            boolean reachedGoal = (goalPiece == currentPiece);
            if (!Pieces.piecesConnect(this.currentBoard.getCell(this.personCoordinates), currentPiece, this.personCoordinates.x(), this.personCoordinates.y(), x, y))
                return this.setFailure(getString(R.string.person_has_no_path));
            if (this.currentBoard.isOnEdge(c) && !reachedGoal)
                return this.setFailure(getString(R.string.person_on_edge_can_only_go_to_their_house));
            //-- it appears that we now have a valid move.
            //-- move the person
            sourcePiece = Pieces.setPersonNumber(sourcePiece,0);
            currentPiece = Pieces.setPersonNumber(currentPiece,thisPersonNumber);
            this.currentBoard.setCell(this.personCoordinates,sourcePiece);
            this.personMovesLeft--;
            this.personCoordinates = c;
            //-- check for coins
            if (Pieces.gold(currentPiece) || Pieces.silver(currentPiece)) {
                if (Pieces.gold(currentPiece)) this.incrementScore(this.pointsForGold());
                if (Pieces.silver(currentPiece)) this.incrementScore(this.pointsForSilver());
                currentPiece = Pieces.setCoins(currentPiece,false,false);
            }
            //-- check for reaching goal
            if (reachedGoal) {
                this.incrementScore(this.personScores[thisPersonNumber]);
                this.personScores[thisPersonNumber]--;
                if (this.personScores[thisPersonNumber]<0) this.personScores[thisPersonNumber] = 0;
                //-- check: have we won by finding all the goals?
                if (this.currentBoard.isWinningBoard()) this.winner = this.currentParticipant;
            }
            //-- now our piece is ready to store and we can return.
            this.currentBoard.setCell(c, currentPiece);
            return Pieces.createSuccessPiece();
        }
        else if (Pieces.isPerson(p) || Pieces.isHouse(p)) {
            // Playing a person piece or house piece when you're not moving a person.
            if (!this.currentBoard.isOnEdge(c)) return this.setFailure(getString(R.string.people_and_houses_edge_only));  // person piece has to be placed on edge
            if (this.currentBoard.distanceToPartner(p, c) < 5) return this.setFailure(getString(R.string.house_person_too_close));  // person piece can't be too close to house piece
            if (!Pieces.isGreenGrassPiece(currentPiece) return this.setFailure(getString(R.string.houses_and_people_only_on_green_grass));  // have to put house or person on green grass.
            // combine the pieces and store the result
            int newPiece = Pieces.combinePieces(currentPiece, p);
            if (Pieces.isFailurePiece(newPiece)) return this.setFailure(getString(R.string.unexpected_problem));
            this.currentBoard.setCell(c, newPiece);
            this.setCurrentPiecePlayed();
            return Pieces.createSuccessPiece();
        } else {
            // Playing a regular piece.  If you play it against an existing piece with a person on it,
            // it starts person-moving mode.  If you play it on a blank square, it places the piece.
            // Other moves are invalid.

        }
    }

    public int playPieceDiscard(int p) {
        //  Discards a piece.  Of course you can't discard people or house pieces, and discarding
        //  doesn't work if you started moving a person.
        //  Returns the success or failure piece.
        if (this.movingPerson) return this.setFailure(getString(R.string.cant_discard_moving_person));
        if (Pieces.isHouse(p)) return this.setFailure(getString(R.string.cant_discard_house));
        if (Pieces.isPerson(p)) return this.setFailure(getString(R.string.cant_discard_person));
        
    }

    public boolean movingPerson() {
        // True if a person is moving as a result of the last play.
        return this.movingPerson;
    }

    public int movingPersonMovesLeft() {
        if (!this.movingPerson) return 0;
        return this.personMovesLeft;
    }

    public WoodsyBoardData getCurrentBoard() {
        // retrieve the current board, which is the one that has any modifications during the turn.
        return this.currentBoard;
    }

    public void endTurn() {
        // call this when the turn is complete, right before serializing the data.
        // it saves the proposed turn into the actual current participant's board.
        int cindx = this.getIndex(this.currentParticipant);
        if (cindx < 0) return;
        this.boards[cindx].copyFrom()
    }

    public String getWinner() {
        // gets the winner participant ID, or "" if no winner yet
        return this.winner;
    }

}
