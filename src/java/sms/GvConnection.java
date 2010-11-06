package sms;

import gvjava.org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.xml.sax.SAXException;

import com.techventus.server.voice.Voice;
import com.techventus.server.voice.exception.CaptchaRequiredException;

import static core.PioText.*;
import core.Query;
import static sms.GoogleXmlParser.*;

/**
 * A connection to send and recieve texts from Google Voice. The setup() method
 * must be called before any other request, and can be called again to refresh
 * the connection. This connection is pull only and must have the
 * getNewMessages() method actively called to recieve messages.
 */
public class GvConnection implements SmsConnection {

	/** The username of the account this connects to. */
	private final String username;

	/** The password to login to the account. */
	private final String password;

	/** The pre-processing connection supplied by the google-voice-java library. */
	private Voice voice;

	/**
	 * Creates a new connection to a Google Voice voice accoutnt from the
	 * supplied arguments. The connection then needs to be setup().
	 * 
	 * @param username
	 *            the username or email addess of the account
	 * @param the
	 *            cleartext password of the account
	 */
	public GvConnection(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public void connect() throws ConnectionException {
		try {
			voice = new Voice(username, password);
		} catch (CaptchaRequiredException e) {
			String notice = String
					.format(
							"A Google Voice captcha is required.\nImage URL  = %s\nCapt Token = %s\n\n",
							e.getCaptchaUrl(), e.getCaptchaToken());
			throw new ConnectionException(notice, e);
		} catch (IOException e) {
			throw new ConnectionException("Unable to connect to Google Voice.",
					e);
		}
	}

	@Override
	public List<Query> getNewMessages() throws SmsRecieveException {
		List<Query> messages = null;
		try {
			String page = voice.getSMS();
			messages = parse(page);
		} catch (IOException e) {
			throw new SmsRecieveException(
					"Could retrieve sms from Google Voice.", e);
		} catch (SAXException e) {
			throw new SmsRecieveException(
					"Could not parse sms xml returned by Google Voice.", e);
		} catch (JSONException e) {
			throw new SmsRecieveException(
					"Could not parse json returned by Google Voice.", e);
		}
		return messages;
	}

	String getRawSmsXml() throws IOException {
		return voice.getSMS();
	}

	@Override
	public void sendSms(String number, String message) throws SmsSendException {
		try {
			voice.sendSMS(number, message);
		} catch (IOException e) {
			throw new SmsSendException(
					"Unable to send sms though Google Voice", e);
		}
	}

	// a simple test that prints all pending queries to the console
	public static void main(String[] args) throws IOException {
		Properties props = load(PROPERTY_FILE);
		GvConnection connection = new GvConnection(
				props.getProperty("gv_user"), props.getProperty("gv_pass"));
		connection.connect();
		// String filename = "resources/gv_dump";
		// for ( int i = 0; i < 5; i++) {
		// utils.FileUtils.writeFile(filename + i + ".xml",
		// connection.getRawSmsXml(), true);
		// }
		List<Query> list = connection.getNewMessages();
		System.out.println("\n" + list.size() + " new queries:");
		for (Query q : list) {
			System.out.println(persistance.Log.queryToString(q));
		}
	}

}
