public interface MessageEventListener
{
	public void connectedToServer();
	public void receivedPeerList(Object[] peerList);
}