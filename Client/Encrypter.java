import java.util.*;
import java.io.*;

public abstract class Encrypter
{
	protected List _listeners = new ArrayList();

	public abstract void encode(String filePath, String outputPath, String k1, String k2, String initVector) throws IOException;
	public abstract void decode(String filePath, String outputPath, String k1, String k2, String initVector) throws IOException;

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