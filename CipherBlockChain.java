//
//  CipherBlockChain.java
//  
import java.io.*;
import java.nio.file.*;


public class CipherBlockChain
{
    public CipherBlockChain()
    {    
    }

    public void encode(String filePath, String k1, String k2, String initVector) throws IOException
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

        FileReader in = new FileReader(filePath);
        int c, count = 0;

        //Decode
        DESRound round = new DESRound();
        byte[] messageSegment = new byte[8];
        String messageSegString;
        String hexString;

        //Open print writer
        PrintWriter out = new PrintWriter("testoutputEncoded.txt");

        //for(int x = 0; x < messageIn.length; x = x + 8)
        while ((c = in.read()) != -1)
        {
            //Reset message segment string
            messageSegString = "";

            messageSegString += (char)c;

            //Already read first byte of block, now read the next seven
            for(int x = 1; x <= 7; ++x)
            {
                if((c = in.read()) != -1)
                    messageSegString += (char)c;
                else
                    messageSegString += " ";
            }

            hexString = DES.toHex(messageSegString);
            d1 = Long.decode("0x" + hexString.substring(0,8)).longValue();
            d2 = Long.decode("0x" + hexString.substring(8,16)).longValue();
            messageSegment = DES.twoLongsTo8ByteArray(d2, d1);

            //XOR with the initialization vector
            messageSegment = DES.XORByteArrays(messageSegment, initializationVector);

            //Triple des
            messageSegment = DES.encode(messageSegment, key1, round);
            messageSegment = DES.decode(messageSegment, key2, round);
            messageSegment = DES.encode(messageSegment, key1, round);

            //Write 64 bits to the out file
            out.print(DES.byteArrayToString(messageSegment));

            //Set IV to the decrypted block for chaining
            initializationVector = messageSegment;
        }

        //Close the out file
        out.close();

        //return outMessage;
    }

    public void decode(String filePath, String k1, String k2, String initVector) throws IOException
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

        FileReader in = new FileReader(filePath);
        int c, count = 0;

        //Decode
        DESRound round = new DESRound();
        byte[] messageSegment = new byte[8];
        byte[] tempInitVector;
        String messageSegString;
        String hexString;
        String outMessage = "";

        //Open print writer
        PrintWriter out = new PrintWriter("testoutputDecoded.txt");

        //for(int x = 0; x < messageIn.length; x = x + 8)
        while ((c = in.read()) != -1)
        {
            //Reset message segment string
            messageSegString = "";

            messageSegString += (char)c;

            //Already read first byte of block, now read the next seven
            for(int x = 1; x <= 15; ++x)
            {
                if((c = in.read()) != -1)
                    messageSegString += (char)c;
                else
                    messageSegString += " ";
            }

            d1 = Long.decode("0x" + messageSegString.substring(0,8)).longValue();
            d2 = Long.decode("0x" + messageSegString.substring(8,16)).longValue();
            messageSegment = DES.twoLongsTo8ByteArray(d2, d1);            
            tempInitVector = messageSegment;

            //Triple des
            messageSegment = DES.decode(messageSegment, key1, round);
            messageSegment = DES.encode(messageSegment, key2, round);
            messageSegment = DES.decode(messageSegment, key1, round);

            //XOR with the initialization vector
            messageSegment = DES.XORByteArrays(messageSegment, initializationVector);

            //Write 64 bits to the out file
            out.print(new String(messageSegment));

            //Set IV to the decrypted block for chaining
            initializationVector = tempInitVector;
        }

        //Close the out file
        out.close();

        //return outMessage;
    }

    static String readFile(String path) throws IOException 
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));

        return new String(encoded);
    }
}