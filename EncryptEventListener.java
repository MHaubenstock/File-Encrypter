public interface EncryptEventListener
{
	public void beganProcessing();
    public void processedData(long bytesProcessed, long totalBytes);
    public void finishedProcessing();
}