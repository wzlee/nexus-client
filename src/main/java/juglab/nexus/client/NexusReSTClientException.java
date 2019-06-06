package juglab.nexus.client;

import org.jboss.resteasy.spi.HttpResponseCodes;

public class NexusReSTClientException extends Exception {

	private static final long serialVersionUID = 7754846161853588131L;

	private String localMessage = null;

	public NexusReSTClientException() {
		super();
	}

	public NexusReSTClientException( String message, Throwable cause ) {
		super( message, cause );
	}

	public NexusReSTClientException( String message ) {
		super( message );
	}

	public NexusReSTClientException( Throwable cause ) {
		super( cause );
	}

	public NexusReSTClientException( int httpStatus ) {

		switch (httpStatus) {
			case (HttpResponseCodes.SC_FORBIDDEN):
				localMessage = "Forbidden request (code " + httpStatus + ")";
			break;
			case (HttpResponseCodes.SC_UNAUTHORIZED):
				localMessage = "Unauthorized request (code " + httpStatus + ")";
			break;
			default:
				localMessage = "Server returned code " + httpStatus;
			break;
		}
	}

	@Override
	public String getMessage() {
		if ( localMessage != null )
			return localMessage;
		else
			return super.getMessage();
	}
}
