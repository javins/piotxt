package sms;

import java.io.IOException;

/** For when those pesky sms just won't go. */
@SuppressWarnings("serial")
public class SmsSendException extends IOException {
	
	public SmsSendException(String expl, Exception cause) {
		super(expl, cause);
	}

}
