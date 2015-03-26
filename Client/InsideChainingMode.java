//
//  InsideChainingMode.java
//  
import java.io.*;
import java.nio.file.*;

public class InsideChainingMode extends Encrypter
{
    public InsideChainingMode()
    {    
    }

    public void encode(String filePath, String outputPath, String k1, String k2, String initVector) throws IOException
    {
        beganProcessing();
        
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

        InputStreamReader in = new InputStreamReader(new FileInputStream(filePath), "iso-8859-1");
        long c, fileSize = new File(filePath).length(), bytesRead = 0;

        //Decode
        DESRound round = new DESRound();
        byte[] messageSegment = new byte[8];
        byte[] interimMessageSeg = new byte[8];
        byte[] interimMessageSeg2 = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        byte[] cipherSegment = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        String messageSegString;

        //Open print writer
        FileOutputStream out = new FileOutputStream(outputPath);

        while ((c = in.read()) != -1)
        {
            ++bytesRead;

            //Reset message segment string
            messageSegString = "";

            messageSegString += String.format("%02x", c);

            //Already read first byte of block, now read the next seven
            for(int x = 1; x <= 7; ++x)
            {
                if((c = in.read()) != -1)
                {
                    messageSegString += String.format("%02x", c);
                    ++bytesRead;
                }
                else
                {
                    messageSegString += String.format("%02x", 32);
                }
            }

            d1 = Long.decode("0x" + messageSegString.substring(0,8)).longValue();
            d2 = Long.decode("0x" + messageSegString.substring(8,16)).longValue();
            messageSegment = DES.twoLongsTo8ByteArray(d2, d1);

            //XOR with the initialization vector
            messageSegment = DES.XORByteArrays(messageSegment, initializationVector);

            //Triple des
            messageSegment = DES.encode(messageSegment, key1, round);

            //Save segments for next link in chain
            interimMessageSeg = messageSegment;
            initializationVector = messageSegment;

            messageSegment = DES.decode(messageSegment, key2, round);

            //XOR messageSegment, interimMessageSegment, and last Cipher segment
            messageSegment = DES.XORByteArrays(DES.XORByteArrays(messageSegment, interimMessageSeg2), cipherSegment);

            messageSegment = DES.encode(messageSegment, key1, round);

            //Set IV to the decrypted block for chaining
            interimMessageSeg2 = interimMessageSeg;
            cipherSegment = messageSegment;

            //Write 64 bits to the out file
            out.write(messageSegment);

            //Trigger event
            processedData(messageSegment, bytesRead, fileSize);
        }

        //Close the out file
        out.close();

        //Trigger finished event
        finishedProcessing();
    }

    public void decode(String filePath, String outputPath, String k1, String k2, String initVector) throws IOException
    {
        beganProcessing();

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

        InputStreamReader in = new InputStreamReader(new FileInputStream(filePath), "iso-8859-1");
        long c, fileSize = new File(filePath).length(), bytesRead = 0;

        //Decode
        DESRound round = new DESRound();
        byte[] messageSegment = new byte[8];
        byte[] tempCipherSeg;
        byte[] tempCipherSeg2 = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        byte[] interimMessageSeg = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        String messageSegString;

        //Open print writer
        FileOutputStream out = new FileOutputStream(outputPath);

        while ((c = in.read()) != -1)
        {
            ++bytesRead;

            //Reset message segment string
            messageSegString = "";

            messageSegString += String.format("%02x", c);

            //Already read first byte of block, now read the next seven
            for(int x = 1; x <= 7; ++x)
            {
                if((c = in.read()) != -1)
                {
                    messageSegString += String.format("%02x", c);
                    ++bytesRead;
                }
                else
                {
                    messageSegString += String.format("%02x", 32);
                }
            }

            d1 = Long.decode("0x" + messageSegString.substring(0,8)).longValue();
            d2 = Long.decode("0x" + messageSegString.substring(8,16)).longValue();
            messageSegment = DES.twoLongsTo8ByteArray(d2, d1);            
            tempCipherSeg = messageSegment;

            //Triple des
            messageSegment = DES.decode(messageSegment, key1, round);
            messageSegment = DES.XORByteArrays(DES.XORByteArrays(interimMessageSeg, messageSegment), tempCipherSeg2);
            messageSegment = DES.encode(messageSegment, key2, round);

            interimMessageSeg = messageSegment;
            tempCipherSeg2 = tempCipherSeg;

            messageSegment = DES.decode(messageSegment, key1, round);

            //XOR with the initialization vector
            messageSegment = DES.XORByteArrays(messageSegment, initializationVector);

            //Set IV to the decrypted block for chaining
            initializationVector = interimMessageSeg;

            //Write 64 bits to the out file
            out.write(messageSegment);

            //Trigger event
            processedData(messageSegment, bytesRead, fileSize);
        }

        //Close the out file
        out.close();

        //Trigger finished event
        finishedProcessing();
    }
}