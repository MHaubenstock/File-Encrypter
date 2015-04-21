import java.util.*;
import java.io.*;

public abstract class Encrypter
{
    protected String incrementalOutputPath;
    protected byte[] incrementalK1;
    protected byte[] incrementalK2;
    protected byte[] incrementalInitializationVector;
    protected byte[] incrementalMessageSegment;
    protected Boolean incrementalEncodingInitialized;
    protected long incrementalBytesRead;
    protected DESRound incrementalRound;
    protected FileOutputStream incrementalOutStream;

	protected List _listeners = new ArrayList();

	public abstract void encode(String filePath, String outputPath, String k1, String k2, String initVector) throws IOException;
	public abstract void decode(String filePath, String outputPath, String k1, String k2, String initVector) throws IOException;
    //public abstract void encodeIncrementally(String nextBlock) throws IOException;
    public abstract void decodeIncrementally(byte[] nextBlock) throws IOException;

    public void initializeIncrementalEncodingOrDecoding(String outputPath, String k1, String k2, String initVector) throws IOException
    {
        if(incrementalEncodingInitialized)
            endIncrementalEncodingOrDecoding();

        //Convert keys and initialization vector to byte arrays
        long d1 = Long.decode("0x" + k1.substring(0,8)).longValue();
        long d2 = Long.decode("0x" + k1.substring(8,16)).longValue();
        incrementalK1 = DES.twoLongsTo8ByteArray(d2, d1);

        d1 = Long.decode("0x" + k2.substring(0,8)).longValue();
        d2 = Long.decode("0x" + k2.substring(8,16)).longValue();
        incrementalK2 = DES.twoLongsTo8ByteArray(d2, d1);

        d1 = Long.decode("0x" + initVector.substring(0,8)).longValue();
        d2 = Long.decode("0x" + initVector.substring(8,16)).longValue();
        incrementalInitializationVector = DES.twoLongsTo8ByteArray(d2, d1);

        //Decode
        byte[] messageSegment = new byte[8];
        String messageSegString;

        //Open print writer
        incrementalOutStream = new FileOutputStream(outputPath);

        incrementalOutputPath = outputPath;
        incrementalRound = new DESRound();
        incrementalBytesRead = 0;

        incrementalEncodingInitialized = false;

        //The beginning of incremental decoding happens when it's initialized
        beganProcessing();
    }

    public void endIncrementalEncodingOrDecoding() throws IOException
    {
        //Perform cleanup
        //Close the out file
        incrementalOutStream.close();

        //Trigger finished event
        finishedProcessing();
    }

	public synchronized void addEventListener(EncryptEventListener listener) 
    {
        _listeners.add(listener);
    }

    public synchronized void removeEventListener(EncryptEventListener listener)  
    {
       _listeners.remove(listener);
    }

    protected synchronized void beganProcessing()
    {
        Iterator i = _listeners.iterator();

        while(i.hasNext())
        {
            ((EncryptEventListener) i.next()).beganProcessing();
        }
    }

    protected synchronized void processedData(byte[] bytes, long bytesProcessed, long totalBytes)
    {
        Iterator i = _listeners.iterator();

        while(i.hasNext())
        {
            ((EncryptEventListener) i.next()).processedData(bytes, bytesProcessed, totalBytes);
        }
    }

    protected synchronized void finishedProcessing()
    {
        Iterator i = _listeners.iterator();

        while(i.hasNext())
        {
            ((EncryptEventListener) i.next()).finishedProcessing();
        }
    }
}