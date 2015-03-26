public interface EncryptEventListener
{
	public void beganProcessing();
    public void processedData(byte[] bytes, long bytesProcessed, long totalBytes);
    public void finishedProcessing();
}