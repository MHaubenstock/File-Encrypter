//
//  CipherBlockChain.java
//  
import java.io.*;

public class CipherBlockChain
{
    public CipherBlockChain()
    {    
    }

    public byte[] encode(String filePath, String k1, String k2, String initVector)
    {
        //Convert keys and initialization vector to byte arrays
        long d1 = Long.decode("0x" + k1.substring(0,8)).longValue();
        long d2 = Long.decode("0x" + k1.substring(8,16)).longValue();
        byte[] key1 = DES.twoLongsTo8ByteArray(d2, d1);

        d1 = Long.decode("0x" + k2.substring(0,8)).longValue();
        d2 = Long.decode("0x" + k2.substring(8,16)).longValue();
        byte[] key2 = DES.twoLongsTo8ByteArray(d2, d1);

        d1 = Long.decode("0x" + initVector.substring(0,8)).longValue();
        d2 = Long.decode("0x" + initVector.substring(8,16)).longValue();
        byte[] initializationVector = DES.twoLongsTo8ByteArray(d2, d1);

        //Create in and out byte arrays
        //TODO: Refactor code to treat the file as a stream so I don't run out of memory
        int fileSize = Files.size(filePath);
        byte[] messageIn = new byte[fileSize + ((8 - (messageIn.length % 8)) % 8)];
        byte[] messageOut = new byte[fileSize + ((8 - (messageIn.length % 8)) % 8)];

        //Add message to array and pad message to make it a multiple of 64 bits
        System.arraycopy(Files.readAllBytes(Paths.get(filePath)), 0, messageIn, 0, fileSize);

        for(int x = fileSize; x < fileSize + ((8 - (messageIn.length % 8)) % 8))
            messageIn[x] = (byte)0x00;

        //Encode
        DESRound round = new DESRound();
        byte[] messageSegment = new byte[8];

        for(int x = 0; x <= messageIn.length; x = x + 8)
        {
            //Get 64 bits
            System.arraycopy(messageIn, x, messageSegment, 0, 8);

            //XOR with the initialization vector
            messageSegment = DES.XORByteArrays(messageSegment, initializationVector);

            //Triple des
            messageSegment = DES.encode(messageSegment, key1, round);
            messageSegment = DES.decode(messageSegment, key2, round);
            messageSegment = DES.encode(messageSegment, key1, round);

            //Write 64 bits to the out message
            System.arraycopy(messageSegment, 0, messageOut, x, 8);

            //Set IV to the previously encrypted block for chaining
            initializationVector = messageSegment;
        }

        return messageOut;
    }

    public byte[] decode(String filePath, String k1, String k2, String initVector)
    {
        //Convert keys and initialization vector to byte arrays
        long d1 = Long.decode("0x" + k1.substring(0,8)).longValue();
        long d2 = Long.decode("0x" + k1.substring(8,16)).longValue();
        byte[] key1 = DES.twoLongsTo8ByteArray(d2, d1);

        d1 = Long.decode("0x" + k2.substring(0,8)).longValue();
        d2 = Long.decode("0x" + k2.substring(8,16)).longValue();
        byte[] key2 = DES.twoLongsTo8ByteArray(d2, d1);

        d1 = Long.decode("0x" + initVector.substring(0,8)).longValue();
        d2 = Long.decode("0x" + initVector.substring(8,16)).longValue();
        byte[] initializationVector = DES.twoLongsTo8ByteArray(d2, d1);

        //Create in and out byte arrays
        //TODO: Refactor code to treat the file as a stream so I don't run out of memory
        int fileSize = Files.size(filePath);
        byte[] messageIn = new byte[fileSize + ((8 - (messageIn.length % 8)) % 8)];
        byte[] messageOut = new byte[fileSize + ((8 - (messageIn.length % 8)) % 8)];

        //Add message to array and pad message to make it a multiple of 64 bits
        System.arraycopy(Files.readAllBytes(Paths.get(filePath)), 0, messageIn, 0, fileSize);

        for(int x = fileSize; x < fileSize + ((8 - (messageIn.length % 8)) % 8))
            messageIn[x] = (byte)0x00;

        //Decode
        DESRound round = new DESRound();
        byte[] messageSegment = new byte[8];

        for(int x = 0; x <= messageIn.length; x = x + 8)
        {
            //Get 64 bits
            System.arraycopy(messageIn, x, messageSegment, 0, 8);

            //Triple des
            messageSegment = DES.decode(messageSegment, key1, round);
            messageSegment = DES.encode(messageSegment, key2, round);
            messageSegment = DES.decode(messageSegment, key1, round);

            //XOR with the initialization vector
            messageSegment = DES.XORByteArrays(messageSegment, initializationVector);

            //Write 64 bits to the out message
            System.arraycopy(messageSegment, 0, messageOut, x, 8);

            //Set IV to the decrypted block for chaining
            System.arraycopy(messageIn, x, initializationVector, 0, 8);
        }

        return messageOut;
    }
}