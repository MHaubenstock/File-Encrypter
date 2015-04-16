public interface ConnectionManagerEventListener
{
	public void openedConnection(String connectionName);
	public void closedConnection(String connectionName);
	public void receivedRequestForPrivateConnection(String requestor, String requestee);
}