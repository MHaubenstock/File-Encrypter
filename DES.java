//
//  DES.java
//  DES
//
//  Created by Keith Irwin on Wed Feb 11 2004.
//  This file is in its final form.  Its code will not change.

import java.math.*;

public class DES {
            
    public static byte[/*8*/] encode( byte[/*8*/] data, byte[/*8*/] key, DESRound round ) {

        // Refine key to 56 bits and store it in a long
        long refinedKey = 0;
        for (int i=0; i<56; i++) {
            refinedKey <<= 1;
            int bit = keyPermutationBits[i] - 1;
            refinedKey |= getBit(7-bit%8,key[bit/8]);
        }

    	// Generate Subkeys
    	byte[][] subKey =  keyGen(refinedKey);

    	// Do Initial Permutation
    	byte[] permutedData = {0,0,0,0,0,0,0,0};
    	for (int i=0; i<64; i++) {
    	    permutedData[i/8] <<= 1;
    	    int loc = initialPermutationBits[i] - 1;
    	    permutedData[i/8] |= getBit(7-(loc%8),data[loc/8]);
    	}

    	// Do rounds.
    	data = permutedData;
    	for (int i = 0; i < 16; i++) {
    	    data = round.doOneRound(data,subKey[i]);
    	}

            // Do swap
    	byte[] reversedData = new byte[8];
    	reversedData[0] = data[4];
    	reversedData[1] = data[5];
    	reversedData[2] = data[6];
    	reversedData[3] = data[7];
    	reversedData[4] = data[0];
    	reversedData[5] = data[1];
    	reversedData[6] = data[2];
    	reversedData[7] = data[3];

            // Do final permutation
    	byte[] finalData = {0,0,0,0,0,0,0,0};
    	for (int i=0; i<64; i++) {
    	    finalData[i/8] <<= 1;
    	    int loc = finalPermutationBits[i] - 1;
    	    finalData[i/8] |= getBit(7-(loc%8),reversedData[loc/8]);
    	}
        
        return finalData;
    }
    
    public static byte[/*8*/] decode( byte[/*8*/] data, byte[/*8*/] key, DESRound round ) {
        // Refine key to 56 bits and store it in a long
        long refinedKey = 0;
        for (int i=0; i<56; i++) {
            refinedKey <<= 1;
            int bit = keyPermutationBits[i] - 1;
            refinedKey |= getBit(7-bit%8,key[bit/8]);
        }

    	// Generate Subkeys
    	byte[][] subKey =  keyGen(refinedKey);

    	// Do Initial Permutation
    	byte[] permutedData = {0,0,0,0,0,0,0,0};
    	for (int i=0; i<64; i++) {
    	    permutedData[i/8] <<= 1;
    	    int loc = initialPermutationBits[i] - 1;
    	    permutedData[i/8] |= getBit(7-(loc%8),data[loc/8]);
    	}

    	// Do rounds.
    	data = permutedData;
    	for (int i = 0; i < 16; i++) {
    	    data = round.doOneRound(data,subKey[15-i]);
    	}

            // Do swap
    	byte[] reversedData = new byte[8];
    	reversedData[0] = data[4];
    	reversedData[1] = data[5];
    	reversedData[2] = data[6];
    	reversedData[3] = data[7];
    	reversedData[4] = data[0];
    	reversedData[5] = data[1];
    	reversedData[6] = data[2];
    	reversedData[7] = data[3];

            // Do final permutation
    	byte[] finalData = {0,0,0,0,0,0,0,0};
    	for (int i=0; i<64; i++) {
    	    finalData[i/8] <<= 1;
    	    int loc = finalPermutationBits[i] - 1;
    	    finalData[i/8] |= getBit(7-(loc%8),reversedData[loc/8]);
    	}

        return finalData;
    }
    
    /** This gets the ind bit starting from the least significant edge.
        We can use this function for longs, ints, chars, or bytes because the lesser
        numeric classes will be upconverted by Java to longs by padding the left side
        with zeros.  Note that this does not correspond to the bit ordering used
        in DES where bits are numbered starting on the most significant side. */
    public static long getBit(int ind, long num)
    {
        num >>= ind;
        num &= 1;
        return num;
    };
    
    /** This converts an array of bytes into a long.  The lower numbered bytes
        are considered to be most significant.  If the array is greater than 8 in
        length, then the most significant bytes will overflow and go away.
        It is also advised to not use more than 7 bytes with this because with 8,
        the most significant bit becomes the sign, and although two's complement
        arthimetic is the standard, there can be issues.  */
    public static long byteArrayToLong( byte[] in ) {
        int len = in.length;
        long out = 0;
        for (int i = 0 ; i < len ; i++) {
            out <<= 8;
            out |= (in[i]&0xff); /* The & 0xff is needed because bytes get upconverted to ints in
		                    expressions and because Java treats bytes as signed quantities.*/
        }
        return out;
    }

    /** This converts a single long into an array of 4 bytes.  The lower bytes
        are the most significant.  If the long is greater than 0xffffffff or
        negative then the bits more siginificant than the 32nd bit are ignored.  */
    public static byte[/*4*/] longTo4ByteArray(long in) {
        byte[] out = new byte[4];
        for (int i=3; i>=0; i--) {
            out[i] = (byte)(in & 0xff);
            in >>= 8;
        }
        return out;
    }

    /** This converts a single long into an array of 6 bytes.  The lower bytes
        are the most significant.  If the long is greater than 0xffffffffffff or
        negative then the bits more siginificant than the 48th bit are ignored.  */
    public static byte[/*6*/] longTo6ByteArray(long in) {
        byte[] out = new byte[6];
        for (int i=5; i>=0; i--) {
            out[i] = (byte)(in & 0xff);
            in >>= 8;
        }
        return out;
    }

    /** This converts two longs into a single array of 8 bytes.  The lower bytes
        are the most significant.  Inlow is the least significant long, inhigh
        the most significant.  If either long is greater than 0xffffffff or negative
        then bits more significant than the 32nd bit are ignored.*/
    public static byte[/*8*/] twoLongsTo8ByteArray(long inlow, long inhigh) {
        byte[] out = new byte[8];
        for (int i=3; i>=0; i--) {
            out[i] = (byte)(inhigh & 0xff);
            inhigh >>= 8;
            out[i+4] = (byte)(inlow & 0xff);
            inlow >>= 8;
        }
        return out;
    }

    public static String byteArrayToString( byte[] in ) {
        int len = in.length;
        String out = "";
        for (int i = 0 ; i < len ; i++) {
            String temp = Long.toHexString(in[i]&0xff);
	    if (temp.length() == 1) temp = "0"+temp;
	    out += temp;
        }
        return out;
    }

    public static byte[][] keyGen(long k)
    {
	byte[][] out = new byte[16][];
	int c,d;
	c = (int)(k >> 28);
	d = (int)(k & 0xfffffff);
	for (int i = 0; i<16; i++)
	{
	    c = rotate(c,shiftAmount[i]);
	    d = rotate(d,shiftAmount[i]);

	    long temp = (((long) c) << 28) | d;
	    out[i] = longTo6ByteArray(keyGenPermuteAndDiscard(temp));
	    
	}
	return out;
    };

    public static int rotate(int x, int n)
    {
	return (x >> (28 - n)) | ((x << n) & 0xfffffff);
    };

    public static long keyGenPermuteAndDiscard(long k)
    {
	long output = 0;
	for (int i=0; i<48; i++)
	{
	    output <<= 1;
	    output |= getBit(55-keyGenBits[i],k);
	}
	return output;
    };

    public static byte[] XORByteArrays(byte[] b1, byte[] b2)
    {
        if(b1.length != b2.length)
        {
            return new byte[1];
        }

        byte[] outArray = new byte[b1.length];

        for(int x = 0; x < b1.length; ++x)
        {
            outArray[x] = (byte)(b1[x] ^ b2[x]);
        }

        return outArray;
    }

    public static String toHex(String arg)
    {
        return String.format("%16x", new BigInteger(1, arg.getBytes()));
    }

    public static int[] keyGenBits = { 13, 16, 10, 23, 0, 4, 2, 27, 14, 5, 20, 9, 22, 18, 11,
                                        3, 25, 7, 15, 6, 26, 19, 12, 1, 40, 51, 30, 36, 46,
                                        54, 29, 39, 50, 44, 32, 47, 43, 48, 38, 55, 33, 52,
                                        45, 41, 49, 35, 28, 31 };

    public static int[][][] SBoxContents =
		{ { { 14,  4, 13,  1,  2, 15, 11,  8,  3, 10,  6, 12,  5,  9,  0,  7 },
		    {  0, 15,  7,  4, 14,  2, 13,  1, 10,  6, 12, 11,  9,  5,  3,  8 },
		    {  4,  1, 14,  8, 13,  6,  2, 11, 15, 12,  9,  7,  3, 10,  5,  0 },
		    { 15, 12,  8,  2,  4,  9,  1,  7,  5, 11,  3, 14, 10,  0,  6, 13 }, },
		  { { 15,  1,  8, 14,  6, 11,  3,  4,  9,  7,  2, 13, 12,  0,  5, 10 },
		    {  3, 13,  4,  7, 15,  2,  8, 14, 12,  0,  1, 10,  6,  9, 11,  5 },
		    {  0, 14,  7, 11, 10,  4, 13,  1,  5,  8, 12,  6,  9,  3,  2, 15 },
		    { 13,  8, 10,  1,  3, 15,  4,  2, 11,  6,  7, 12,  0,  5, 14,  9 }, },
		  { { 10,  0,  9, 14,  6,  3, 15,  5,  1, 13, 12,  7, 11,  4,  2,  8 },
		    { 13,  7,  0,  9,  3,  4,  6, 10,  2,  8,  5, 14, 12, 11, 15,  1 },
		    { 13,  6,  4,  9,  8, 15,  3,  0, 11,  1,  2, 12,  5, 10, 14,  7 },
		    {  1, 10, 13,  0,  6,  9,  8,  7,  4, 15, 14,  3, 11,  5,  2, 12 }, },
		  { {  7, 13, 14,  3,  0,  6,  9, 10,  1,  2,  8,  5, 11, 12,  4, 15 },
		    { 13,  8, 11,  5,  6, 15,  0,  3,  4,  7,  2, 12,  1, 10, 14,  9 },
		    { 10,  6,  9,  0, 12, 11,  7, 13, 15,  1,  3, 14,  5,  2,  8,  4 },
		    {  3, 15,  0,  6, 10,  1, 13,  8,  9,  4,  5, 11, 12,  7,  2, 14 }, },
		  { {  2, 12,  4,  1,  7, 10, 11,  6,  8,  5,  3, 15, 13,  0, 14,  9 },
		    { 14, 11,  2, 12,  4,  7, 13,  1,  5,  0, 15, 10,  3,  9,  8,  6 },
		    {  4,  2,  1, 11, 10, 13,  7,  8, 15,  9, 12,  5,  6,  3,  0, 14 },
		    { 11,  8, 12,  7,  1, 14,  2, 13,  6, 15,  0,  9, 10,  4,  5,  3 }, },
		  { { 12,  1, 10, 15,  9,  2,  6,  8,  0, 13,  3,  4, 14,  7,  5, 11 },
		    { 10, 15,  4,  2,  7, 12,  9,  5,  6,  1, 13, 14,  0, 11,  3,  8 },
		    {  9, 14, 15,  5,  2,  8, 12,  3,  7,  0,  4, 10,  1, 13, 11,  6 },
		    {  4,  3,  2, 12,  9,  5, 15, 10, 11, 14,  1,  7,  6,  0,  8, 13 }, },
		  { {  4, 11,  2, 14, 15,  0,  8, 13,  3, 12,  9,  7,  5, 10,  6,  1 },
		    { 13,  0, 11,  7,  4,  9,  1, 10, 14,  3,  5, 12,  2, 15,  8,  6 },
		    {  1,  4, 11, 13, 12,  3,  7, 14, 10, 15,  6,  8,  0,  5,  9,  2 },
		    {  6, 11, 13,  8,  1,  4, 10,  7,  9,  5,  0, 15, 14,  2,  3, 12 }, },
		  { { 13,  2,  8,  4,  6, 15, 11,  1, 10,  9,  3, 14,  5,  0, 12,  7 },
		    {  1, 15, 13,  8, 10,  3,  7,  4, 12,  5,  6, 11,  0, 14,  9,  2 },
		    {  7, 11,  4,  1,  9, 12, 14,  2,  0,  6, 10, 13, 15,  3,  5,  8 },
		    {  2,  1, 14,  7,  4, 10,  8, 13, 15, 12,  9,  0,  3,  5,  6, 11 }, }, };

    /* Bits used for the permutation in the round. */
    public static int[] permutationBits = { 15, 6, 19, 20, 28, 11, 27, 16, 0, 14, 22, 25, 4, 17,
                                            30, 9, 1, 7, 23, 13 ,31, 26, 2, 8, 18, 12, 29, 5, 21,
                                            10, 3, 24 }; 
                                            
    public static int[] keyPermutationBits = { 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18,
                                               10, 2, 59, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36,
                                               63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22,
                                               14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4 };
    
    public static int[] subkeyPermutationBits =
    {   14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10,
        23, 19, 12, 4, 26, 8, 16, 7, 27, 20, 13, 2,
        41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48,
        44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32 };
                                                                                                                                     
    public static int[] initialPermutationBits =
    {   58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17,  9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7 };
    
    public static int[] finalPermutationBits =
    {   40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41,  9, 49, 17, 57, 25 };

    public static int[] shiftAmount = { 1,1,2,2,2,2,2,2,1,2,2,2,2,2,2,1 };
}
