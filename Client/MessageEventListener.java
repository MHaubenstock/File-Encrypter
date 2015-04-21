public interface MessageEventListener
{
	public void connectedToServer();
	public void receivedPeerList(Object[] peerList);
	public void startFileTransfer(String peer);
	public void receivedFileSendRequest(String fileName, String k1, String k2, String iv);
	public void recievedDataBlockFromPeer(String peer, String dataBlock);
	public void endedFileTransferWithPeer(String peer);
}