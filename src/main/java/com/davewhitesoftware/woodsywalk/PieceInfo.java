package com.davewhitesoftware.woodsywalk;

import java.util.Random;


//
//  PieceInfo: An object with only static methods (so don't instantiate it, no need)
//  that gives information about the pieces in the game-- the list of pieces and methods
//  to discover information about each piece.
//
//  Each piece is an 18-bit positive integer, with the following bitmapped meanings:
//  Bits 17-15:     0 if no house figure is on this piece, or 1,2,3,4 for the color of the house
//  Bits 14-12:     0 if no person figure is on this piece, or 1,2,3,4 for the color of the person
//  Bits 11-6:      The piece number, 1 through the number of pieces (currently 36, can range up to 64).
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

public class PieceInfo {
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
        PieceInfo.shuffleArray(pieceArray);
        return pieceArray;
    }

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

    // Static method for creating a piece (which is of course actually an integer)
    public static int createPiece(boolean up, boolean down, boolean left, boolean right, boolean silver, boolean gold, int pieceNumber, int personNumber, int houseNumber) {
        int p = 0;
        if (up) p |= 0b100000;
        if (down) p |= 0b10000;
        if (left) p |= 0b1000;
        if (right) p |= 0b100;
        if (silver) p |= 0b10;
        if (gold) p |= 0b1;
        p |= (pieceNumber << 6);
        p |= (personNumber << 12);
        p |= (houseNumber << 15);
        return p;
    }

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
}
