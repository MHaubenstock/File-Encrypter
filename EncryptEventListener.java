public interface EncryptEventListener
{
    public void processedData(int bytesProcessed, int totalBytes);
    public void finishedProcessing();
}