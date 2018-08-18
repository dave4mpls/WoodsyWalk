package com.davewhitesoftware.woodsywalk;

import java.util.Random;


//
//  Pieces: An object with only static methods (so don't instantiate it, no need)
//  that gives information about the pieces in the game-- the list of pieces and methods
//  to discover information about each piece.
//
//  Each piece is an 18-bit positive integer, with the following bitmapped meanings:
//  Bits 17-15:     0 if no house figure is on this piece, or 1,2,3,4 for the color of the house
//  Bits 14-12:     0 if no person figure is on this piece, or 1,2,3,4 for the color of the person
//  Bits 11-6:      The piece number, 1 through the number of pieces (currently 36, can range up to 63).
//                  A piece that is all zeroes is blank.
//                  A piece that has a piece number of 63 and no other data represents End of Turn in the game data's piece array.
//                  A piece with a piece number of 62 and no other data represents Success when playing a piece.
//                  A piece with a piece number of 61 and no other data represents Invalid Move when playing a piece.
//                  A piece with a piece number of 60 and no directions/coins is a regular piece, that displays as just green grass.   You can add a person or house to it.
//  Bit 5:          Set if the piece has an upward line.
//  Bit 4:          Set if the piece has a downward line.
//  Bit 3:          Set if the piece has a leftward line.
//  Bit 2:          Set if the piece has a rightward line.
//  Bit 1:          Set if the piece has a silver coin.
//  Bit 0:          Set if the piece has a gold coin.
//
//  You don't need to know that, though, just call the methods in this class to process the pieces.
//  The pieces are NOT exactly the same as any other game that may have inspired this one!
//

public class Pieces {
    public static void shuffleArray(int[] ar) {
        // Fisher-Yates shuffle as shown in Stack Overflow: https://stackoverflow.com/questions/1519736/random-shuffling-of-an-array
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public static int[] pieces() {
        // Returns the list of all pieces.
        // The pieces are shuffled randomly before being returned.
        // Note that since they are expressed in binary, you can more easily modify them using the definition above.
        int[] pieceArray = new int[] {
                ((1 << 6) | 0b001100), ((7 << 6) | 0b001100), ((13 << 6) | 0b011110), ((19 << 6) | 0b011010), ((25 << 6) | 0b011100), ((31 << 6) | 0b011100),
                ((2 << 6) | 0b111100), ((8 << 6) | 0b111100), ((14 << 6) | 0b100110), ((20<< 6) | 0b101010), ((26<< 6) | 0b101110), ((32<< 6) | 0b101100),
                ((3 << 6) | 0b001110), ((9<< 6) | 0b001101), ((15<< 6) | 0b001110), ((21<< 6) | 0b001101), ((27<< 6) | 0b110000), ((33<< 6) | 0b110000),
                ((4 << 6) | 0b001100), ((10<< 6) | 0b001100), ((16<< 6) | 0b010000), ((22<< 6) | 0b001000), ((28<< 6) | 0b110100), ((34<< 6) | 0b111000),
                ((5 << 6) | 0b111100), ((11<< 6) | 0b111100), ((17<< 6) | 0b100110), ((23<< 6) | 0b101000), ((29<< 6) | 0b110100), ((35<< 6) | 0b111000),
                ((6 << 6) | 0b110010), ((12<< 6) | 0b110001), ((18<< 6) | 0b110010), ((24<< 6) | 0b110001), ((30<< 6) | 0b110000), ((36<< 6) | 0b110000)
        };
        Pieces.shuffleArray(pieceArray);
        return pieceArray;
    }

    // Number of people and house pairs in the game
    public static int numberOfPeople() { return 4; }

    // Static methods for finding directional status or coin status for a piece.
    public static boolean up(int p) { return ((p & 0b100000) != 0); }
    public static boolean down(int p) { return ((p & 0b10000) != 0); }
    public static boolean left(int p) { return ((p & 0b1000) != 0); }
    public static boolean right(int p) { return ((p & 0b100) != 0); }
    public static boolean silver(int p) { return ((p & 0b10) != 0); }
    public static boolean gold(int p) { return ((p & 1) != 0); }
    public static int pieceNumber(int p) { return ((p >> 6) & 0b111111); }
    public static int personNumber(int p) { return ((p >> 12) & 0b111); }
    public static int houseNumber(int p) { return ((p >> 15) & 0b111); }
    public static boolean isHouse(int p) {      // true if the piece is JUST a house piece (all the rest zero)
        return ((p & 0b000111111111111111) == 0);
    }
    public static boolean isPerson(int p) {     // true if the piece is JUST a person piece (all the rest zero)
        return ((p & 0b111000111111111111) == 0);
    }
    public static boolean isPersonAndHouse(int p) {     // true if the piece is a person AND house
        return ((p & 0b000000111111111111) == 0);
    }
    public static boolean isTile(int p) {       // true if the piece is any other piece but a plain person or house piece
        return ((!Pieces.isHouse(p)) && (!Pieces.isPerson(p)) && (!Pieces.isPersonAndHouse(p)));
    }
    public static boolean isBlank(int p) { return (p==0); }
    public static boolean isEndOfTurn(int p) { return (Pieces.pieceNumber(p)==63); }
    public static int numberMoves(int p) {
        // Returns the number of moves you can make of a person piece by discarding this piece.
        return ((Pieces.up(p)? 1 : 0) + (Pieces.down(p)? 1: 0) + (Pieces.left(p)? 1: 0)
            + (Pieces.right(p)? 1: 0));
    }

    public static boolean piecesConnect(int a, int b, int ax, int ay, int bx, int by) {
        // Returns true if the given pieces, with the given coordinates (in any arbitrary grid)
        // have connected paths.  In that case, it is permissible to move a person to that square.
        // First, are the pieces adjacent?
        if (Math.abs(ax-bx) > 1) return false;
        if (Math.abs(ay-by) > 1) return false;
        // Are they on the same square?
        if (ax==bx && ay==by) return false;
        // We know the pieces are adjacent, so check the path lines.
        if (ax==bx && ay<by && Pieces.down(a) && Pieces.up(b)) return true;
        if (ax==bx && ay>by && Pieces.up(a) && Pieces.down(b)) return true;
        if (ay==by && ax<bx && Pieces.right(a) && Pieces.left(b)) return true;
        if (ay==by && ax>bx && Pieces.left(a) && Pieces.right(b)) return true;
        // Diagonals, etc. -- Not connected.
        return false;
    }

    public static int setDirections(int p, boolean up, boolean down, boolean left, boolean right) {
        // Returns a piece with directions set.
        if (up) p |= 0b100000;
        if (down) p |= 0b10000;
        if (left) p |= 0b1000;
        if (right) p |= 0b100;
        return p;
    }

    public static int setCoins(int p, boolean silver, boolean gold) {
        if (silver) p |= 0b10;
        if (gold) p |= 0b1;
        return p;
    }

    // Static method for creating a piece (which is of course actually an integer)
    public static int createPiece(boolean up, boolean down, boolean left, boolean right, boolean silver, boolean gold, int pieceNumber, int personNumber, int houseNumber) {
        int p = 0;
        p = Pieces.setDirections(p,up,down,left,right);
        p = Pieces.setCoins(p,silver,gold);
        p |= (pieceNumber << 6);
        p |= (personNumber << 12);
        p |= (houseNumber << 15);
        return p;
    }

    public static int createHousePiece(int h) {
        return Pieces.createPiece(false,false,false,false,false,false,0,0,h);
    }

    public static int createPersonPiece(int p) {
        return Pieces.createPiece(false,false,false,false,false,false,0,p,0);
    }


    public static int createBlankPiece() { return 0; }
    public static int createEndOfTurnPiece() {
        return Pieces.createPiece(false,false,false,false,false,false,63,0,0);
    }
    public static int createSuccessPiece() {
        return Pieces.createPiece(false,false,false,false,false,false,62,0,0);
    }
    public static int createFailurePiece() {
        return Pieces.createPiece(false,false,false,false,false,false,61,0,0);
    }
    public static int createGreenGrassPiece() {
        return Pieces.createPiece(false,false,false,false,false,false,60,0,0);
    }
    public static int getPersonPieceFrom(int p) {
        // given a piece with a person on it, get the corresponding person piece.
        // Returns the failure piece if no person is on it.
        if (Pieces.personNumber(p)==0) return createFailurePiece();
        return createPersonPiece(Pieces.personNumber(p));
    }
    public static int getHousePieceFrom(int p) {
        // given a piece with a house on it, get the corresponding house piece.
        // Returns the failure piece if no house is on it.
        if (Pieces.houseNumber(p) == 0) return createFailurePiece();
        return createHousePiece(Pieces.houseNumber(p));
    }
    public static boolean isEndOfTurnPiece(int p) { return p == Pieces.createEndOfTurnPiece(); }
    public static boolean isSuccessPiece(int p) { return p == Pieces.createSuccessPiece(); }
    public static boolean isFailurePiece(int p) { return p == Pieces.createFailurePiece(); }
    public static boolean isGreenGrassPiece(int p) { return p == Pieces.createGreenGrassPiece(); }

    // Methods for modifying a piece
    public static int takeCoins(int p) {
        // take the coins off a piece
        return (p & 0b111111111111111100);
    }
    public static int setHouseNumber(int p, int houseNumber) {
        // set the house number for a piece (i.e. add or remove a house)
        return (p & 0b000111111111111111) | ((houseNumber & 0b111) << 15);
    }
    public static int setPersonNumber(int p, int personNumber) {
        // set the person number for a piece (i.e. add or remove a person)
        return (p & 0b111000111111111111) | ((personNumber & 0b111) << 12);
    }
    public static int combinePieces(int p1, int p2) {
        // Combines a piece that is a person or house, with a regular piece.  Order is unimportant.
        // Returns the failure piece if neither piece is a plain person or house.
        if (Pieces.isTile(p1)) { int t = p1; p1 = p2; p2 = t; }
        if (Pieces.isPerson(p1) && Pieces.isTile(p2)) return Pieces.setPersonNumber(p2, Pieces.personNumber(p1));
        if (Pieces.isHouse(p1) && Pieces.isTile(p2)) return Pieces.setHouseNumber(p2, Pieces.houseNumber(p1));
        return Pieces.createFailurePiece();
    }

}
