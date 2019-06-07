package juglab.nexus.client;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;

import org.jboss.resteasy.spi.HttpResponseCodes;

public class NexusReSTClientException extends Exception {

	private static final long serialVersionUID = 7754846161853588131L;

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

	/**
	 * Convenience method to facilitate feedback to user
	 * 
	 * @return an HTTP Error Code if exception was caused by a server error, else returns 0
	 * @see org.jboss.resteasy.util.HttpResponseCodes
	 */
	public int getHttpErrorCode() {
		if ( getCause() instanceof NotFoundException)
			return HttpResponseCodes.SC_NOT_FOUND;
		if ( getCause() instanceof ForbiddenException)
			return HttpResponseCodes.SC_FORBIDDEN;
		if ( getCause() instanceof NotAuthorizedException)
			return HttpResponseCodes.SC_UNAUTHORIZED;
		else
			return 0;
	}
}
