package com.davewhitesoftware.woodsywalk;

//
//  Class to represent an entire game of Woodsy Walk.  The toByteArray method produces a
//  serialized version which is what we pass to Google Play Game Services as our game state.
//  So this object should contain everything needed to play a game, by passing back and forth
//  all the pieces in their shuffled order, which piece we are on, everyone's game boards,
//  which colors of houses and people pieces have been placed, etc.
//


import java.io.Serializable;
import java.util.ArrayList;

public class WoodsyGameData implements Serializable {
    private int numParticipants;
    private ArrayList<String> participantIds;
    private int winner = -1;        // when a game is completed, this is the index of the winner in the participant array above
    private int[] piecesToPlace;                // all the pieces to place, with End of Turn between turns
    private int pindx;                          // index into piecesToPlace for the current position
    private WoodsyBoardData[] boards;       // all the boards for all the players
    private int[] scores;                   // all the player scores

    private transient String currentParticipant;
    private transient WoodsyBoardData currentBoard;   // the current board while we are making changes


    WoodsyGameData(ArrayList<String> inputParticipantIds) {
        // Constructor: create a Woodsy game data structure based on the supplied player ID's.
        this.participantIds.clear();
        for(String thisParticipantId : inputParticipantIds) {
            this.participantIds.add(thisParticipantId);
        }
        this.numParticipants = inputParticipantIds.size();
        this.boards = new WoodsyBoardData[this.numParticipants];
        this.scores = new int[this.numParticipants];
        for (int i = 0; i < this.numParticipants; i++) this.scores[i] = 0;
        // now we need to map out all the pieces that will be played throughout the game.
        // first the person and house pieces (one person and one house per turn),
        // then the regular pieces, one per turn.
        this.pindx = 0;
        int[] rawPieces = PieceInfo.pieces();
        int piecesToPlaySize = 12 + rawPieces.length * 2 * this.numParticipants;
        if (this.numParticipants == 2) piecesToPlaySize -= 2;  // different person/house placing method for 2 players saves 2 array locations
        this.piecesToPlace = new int[piecesToPlaySize];
        int n = 0;
        //--- first, create all the turns where people place people and houses.  Note that
        //--- for a two player game, you place a pair of people and houses before your turn is up.
        if (this.numParticipants == 2) {
            for (int hpi = 1; hpi <= 4; hpi+=2) {
                this.piecesToPlace[n] = PieceInfo.createPersonPiece(hpi); n++;
                this.piecesToPlace[n] = PieceInfo.createHousePiece(hpi); n++;
                this.piecesToPlace[n] = PieceInfo.createPersonPiece(hpi+1); n++;
                this.piecesToPlace[n] = PieceInfo.createHousePiece(hpi+1); n++;
                this.piecesToPlace[n] = PieceInfo.createEndOfTurnPiece(); n++;
            }
        } else {
            for (int hpi = 1; hpi <= 4; hpi++) {
                this.piecesToPlace[n] = PieceInfo.createPersonPiece(hpi); n++;
                this.piecesToPlace[n] = PieceInfo.createHousePiece(hpi); n++;
                this.piecesToPlace[n] = PieceInfo.createEndOfTurnPiece(); n++;
            }
        }
        //--- next, generate all the turns where players place regular pieces.
        for (int rpi = 0; rpi < rawPieces.length; rpi++) {
            for (int j = 0; j < this.numParticipants; j++) {
                this.piecesToPlace[n] = rawPieces[rpi]; n++;
                this.piecesToPlace[n] = PieceInfo.createEndOfTurnPiece(); n++;
            }
        }
    }

    // private methods
    private int getIndex(String participantId) {
        // returns the index of a participant ID or -1 if not found
        for (int i = 0; i < this.numParticipants; i++) {
            if (this.participantIds.get(i).equals(participantId)) return i;
        }
        return -1;
    }

    // methods for accessing data and modifying it during the game
    public WoodsyBoardData getBoard(String participantId) {
        // returns the board corresponding to a participant ID, or null if not found
        int i = this.getIndex(participantId);
        if (i < 0) return null;
        return this.boards[i];
    }

    public void beginTurn(String participantId) {
        // Begin the current turn by copying the
    }

    public WoodsyBoardData getCurrentBoard() {
        // after
    }

    public String getWinner() {
        // gets the winner participant ID, or "" if no winner yet
        if (this.winner < 0) return "";
        return this.participantIds.get(this.winner);
    }

    public int getNextPiece() {
        // returns
    }
}
