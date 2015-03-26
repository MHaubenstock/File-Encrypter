import java.util.*;

public class DESRound
{
	public byte[/*8*/] doOneRound(byte[/*8*/] data, byte[/*6*/] key)
	{
		byte[] leftBits = Arrays.copyOfRange(data, 0, data.length / 2);
		byte[] rightBits = Arrays.copyOfRange(data, data.length / 2, data.length);
		byte[] feistelOutBits = feistelBlock(rightBits, key);
		byte[] rightBitsPrime = new byte[feistelOutBits.length];

		//XOR feistel bits and left bits
		for(int x = 0; x < rightBitsPrime.length; ++x)
			rightBitsPrime[x] = (byte)(feistelOutBits[x] ^ leftBits[x]);
	
		return concatenateByteArrays(rightBits, rightBitsPrime);
	}

	public byte[] feistelBlock(byte[] data, byte[] key)
	{
		byte[] feistelBits = expansion(data);
		
		//XOR feistelBits with key
		for(int x = 0; x < feistelBits.length; ++x)
			feistelBits[x] = (byte)(feistelBits[x] ^ key[x]);

		feistelBits = substitution(feistelBits);

		feistelBits = permutation(feistelBits);

		return feistelBits;
	}

	public byte[] expansion(byte[] data)
	{
		char[] dataBits = bytesToBits(data);
		char[] expansionBits = new char[(int)(dataBits.length * 1.5)];

		//Expand the data
		int expansionBitIndex = 0;

		expansionBits[expansionBitIndex] = dataBits[dataBits.length - 1];
		++expansionBitIndex;

		for(int x = 0; x < dataBits.length; ++x)
		{
			if(x % 4 == 0 && x > 0)
			{
				expansionBits[expansionBitIndex] = dataBits[x];
				++expansionBitIndex;

				expansionBits[expansionBitIndex] = dataBits[x - 1];
				++expansionBitIndex;
			}

			expansionBits[expansionBitIndex] = dataBits[x];
			++expansionBitIndex;
		}

		expansionBits[expansionBitIndex] = dataBits[0];
		++expansionBitIndex;

		return bitsToBytes(expansionBits);
	}

	public byte[] substitution(byte[] data)
	{
		char[] dataBits = bytesToBits(data);
		char[] substitutionBits = new char[(int)(dataBits.length / 1.5)];
		char[] sboxNibble;
		int row, col;

		//Perform substitution
		for(int x = 0; x < dataBits.length / 6; ++x)
		{
			row = Integer.parseInt("" + dataBits[x * 6] + dataBits[(x * 6) + 5], 2);
			col = Integer.parseInt("" + dataBits[(x * 6) + 1] + dataBits[(x * 6) + 2] + dataBits[(x * 6) + 3] + dataBits[(x * 6) + 4], 2);

			sboxNibble = String.format("%4s", Integer.toBinaryString((byte)DES.SBoxContents[x][row][col] & 0xF)).replace(' ', '0').toCharArray();
			System.arraycopy(sboxNibble, 0, substitutionBits, x * 4, 4);
		}

		return bitsToBytes(substitutionBits);
	}

	public byte[] permutation(byte[] data)
	{
		char[] dataBits = bytesToBits(data);
		char[] permutationBits = new char[dataBits.length];

		for(int x = 0; x < permutationBits.length; ++x)
		{
			permutationBits[x] = dataBits[DES.permutationBits[x]];
		}

		return bitsToBytes(permutationBits);
	}

	public byte[] concatenateByteArrays(byte[] array1, byte[] array2)
	{
		byte[] newByteArray = new byte[array1.length + array2.length];

		System.arraycopy(array1, 0, newByteArray, 0, array1.length);
		System.arraycopy(array2, 0, newByteArray, array1.length, array2.length);

		return newByteArray;		
	}

	public char[] bytesToBits(byte[] bytes)
	{
		String bits = "";

		//Expand bytes into individual bits
		for (byte b : bytes)
		{
			bits += String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
		}

		return bits.toCharArray();
	}

	public byte[] bitsToBytes(char[] bits)
	{
		byte[] bytes = new byte[bits.length / 8];

		for(int x = 0; x < bits.length / 8; ++x)
			bytes[x] = (byte)Integer.parseInt(new String(Arrays.copyOfRange(bits, x * 8, (x * 8) + 8)), 2);

		return bytes;
	}
}