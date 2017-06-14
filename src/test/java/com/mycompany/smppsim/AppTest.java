package com.mycompany.smppsim;

import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import com.logica.smpp.Connection;
import com.logica.smpp.Data;
import com.logica.smpp.NotSynchronousException;
import com.logica.smpp.Session;
import com.logica.smpp.TCPIPConnection;
import com.logica.smpp.TimeoutException;
import com.logica.smpp.WrongSessionStateException;
import com.logica.smpp.pdu.BindReceiver;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindTransciever;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.DeliverSM;
import com.logica.smpp.pdu.EnquireLinkResp;
import com.logica.smpp.pdu.PDU;
import com.logica.smpp.pdu.PDUException;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.SubmitSMResp;
import com.logica.smpp.pdu.UnbindResp;
import com.logica.smpp.util.ByteBuffer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import tests.exceptions.BindReceiverException;
import tests.exceptions.BindTransceiverException;
import tests.exceptions.BindTransmitterException;
import tests.exceptions.SubmitSmFailedException;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
	

	String smppAccountName = "smppclient1";
	String smppPassword = "password";
	String smppSystemType = "tests";
	String smppAddressRange = "[0-9]";
	boolean txBound;
	boolean rxBound;
	Session session;
	String smppHost = "192.168.174.136";
	int smppPort = 2775;
	
	
	String smppServiceType = "tests";
	String srcAddress = "12345";
	String destAddress = "4477805432122";
	
	private static Logger logger = Logger.getLogger("smppsim.tests");
	 private static org.slf4j.Logger logger1 = LoggerFactory.getLogger("test");
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    
    public void test001BindTransmitter() throws BindTransmitterException {
		logger.info("Attempting to establish transmitter session");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransmitter();
			breq.setSystemId(smppAccountName);
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			throw new BindTransmitterException(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
		}
		assertEquals(
			"BindTransmitter failed: response was not ESME_ROK",
			Data.ESME_ROK,
			resp.getCommandStatus());
		logger.info("Established transmitter session successfully");
		// Now unbind and disconnect ready for the next test
		try {
			session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}
    
    
    public void test002UnBind() throws BindTransmitterException {
		// First bind so that we can test unbinding
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransmitter();
			breq.setSystemId(smppAccountName);
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			throw new BindTransmitterException(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
		}
		assertEquals(
			"BindTransmitter failed: response was not ESME_ROK",
			Data.ESME_ROK,
			resp.getCommandStatus());
		logger.info("Established transmitter session successfully");
		txBound = true;
		// Now on to the unbind test

		try {
			assertTrue(
				"BindTransmitter test must have failed so cannot unbind",
				txBound);
			logger.info("Connection is currently=" + conn);
			UnbindResp response = session.unbind();
			logger.info("Connection after unbind is=" + conn);
			assertEquals(
				"Unbind failed: response was not ESME_ROK",
				Data.ESME_ROK,
				response.getCommandStatus());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
			fail("Failed to unbind transmitter session " + e.getMessage());
		}
	}

	/**
	 *
	 * Condition: Bind with incorrect systemid
	 * Expected: Operation returns response with cmd_status=ESME_RINVSYSID = 0x0F
	 */

	public void test003BindTransmitterInvalidSystemID()
		throws BindTransmitterException {
		logger.info(
			"Attempting to establish transmitter session with invalid systemid");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransmitter();
			breq.setSystemId("banana");
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			throw new BindTransmitterException(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
		}
		// Response object is null!
		assertEquals(
			"BindTransmitter authentication should have failed: response was not ESME_RINVSYSID",
			Data.ESME_RINVSYSID,
			resp.getCommandStatus());
		// Unbind and disconnect only if this test failed (and we bound)ready for the next test
		if (resp.getCommandStatus() == Data.ESME_ROK) {
			try {
				session.unbind();
				logger.info("Done unbind");
			} catch (Exception e) {
				logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
				logger.warning(
					"Unbind operation failed for TX session. "
						+ e.getMessage());
			}
		}
	}

	/**
	 *
	 * Condition: Bind with incorrect password
	 * Expected: Operation returns response with cmd_status=ESME_RINVPASWD = 0x0E
	 */

	public void test004BindTransmitterInvalidPassword()
		throws BindTransmitterException {
		logger.info(
			"Attempting to establish transmitter session with invalid password");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransmitter();
			breq.setSystemId(smppAccountName);
			breq.setPassword("banana");
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			throw new BindTransmitterException(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
		}
		// Response object is null!
		assertEquals(
			"BindTransmitter authentication should have failed: response was not ESME_RINVPASWD",
			Data.ESME_RINVPASWD,
			resp.getCommandStatus());
		txBound = true;
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}

	/**
	 *
	 * Condition: Bind twice with no intermediate unbind
	 * Expected: Operation returns response with cmd_status=ESME_RALYBND
	 */

	//	public void test005BindTransmitterTwice()
	//		throws BindTransmitterException {
	//			// TODO add debug to API to see if state checking really disabled and whether
	//			// state violation occuring
	//		Response resp = null;
	//		Connection conn;
	//		BindRequest breq;
	//		Session.getDebug().activate();
	//		try {
	//			conn = new TCPIPConnection(smppHost, smppPort);
	//			session = new Session(conn);
	//			breq = new BindTransmitter();
	//			breq.setSystemId(smppAccountName);
	//			breq.setPassword(smppPassword);
	//			breq.setInterfaceVersion((byte) 0x34);
	//			breq.setSystemType(smppSystemType);
	//			resp = session.bind(breq);
	//		} catch (Exception e) {
	//			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
	//			logger.warning(
	//				"Exception whilst setting up or executing bind transmitter. "
	//					+ e.getMessage());
	//			fail(
	//				"Exception whilst setting up or executing bind transmitter. "
	//					+ e.getMessage());
	//			throw new BindTransmitterException(
	//				"Exception whilst setting up or executing bind transmitter. "
	//					+ e.getMessage());
	//		}
	//		assertEquals(
	//			"BindTransmitter authentication should have passed: response was not ESME_ROK",
	//			Data.ESME_ROK,
	//			resp.getCommandStatus());
	//		txBound = true;
	//		try {
	//			session.disableStateChecking();
	//			logger.info("Disabled state checking ready for second bind attempt");
	//			// try to bind again
	//			resp = session.bind(breq);
	//			// TODO resp is null. Is this an API bug?
	//			logger.info("Executed second bind, resp="+resp);
	//		} catch (Exception e) {
	//			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
	//			logger.warning(
	//				"Exception whilst setting up or executing 2nd bind transmitter. "
	//					+ e.getMessage());
	//			fail(
	//				"Exception whilst setting up or executing 2nd bind transmitter. "
	//					+ e.getMessage());
	//			throw new BindTransmitterException(
	//				"Exception whilst setting up or executing 2nd bind transmitter. "
	//					+ e.getMessage());
	//		}
	//		assertEquals(
	//			"BindTransmitter authentication should have failed: response was not ESME_RALYBND",
	//			Data.ESME_RALYBND,
	//			resp.getCommandStatus());
	//		txBound = true;
	//
	//		// Now unbind and disconnect ready for the next test
	//		try {
	//			UnbindResp response = session.unbind();
	//		} catch (Exception e) {
	//			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
	//			logger.warning(
	//				"Unbind operation failed for TX session. " + e.getMessage());
	//		}
	//		Session.getDebug().deactivate();
	//	}

	public void test011BindReceiver() throws BindReceiverException {
		logger.info("Attempting to establish receiver session");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindReceiver();
			breq.setSystemId(smppAccountName);
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			breq.setAddressRange(smppAddressRange);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind receiver. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind receiver. "
					+ e.getMessage());
			throw new BindReceiverException(
				"Exception whilst setting up or executing bind receiver. "
					+ e.getMessage());
		}
		assertEquals(
			"BindReceiver failed: response was not ESME_ROK",
			Data.ESME_ROK,
			resp.getCommandStatus());
		logger.info("Established receiver session successfully");
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for RX session. " + e.getMessage());
		}
	}

	/**
	 *
	 * Condition: Bind with incorrect systemid
	 * Expected: Operation returns response with cmd_status=ESME_RINVSYSID = 0x0F
	 */

	public void test013BindReceiverInvalidSystemID()
		throws BindReceiverException {
		logger.info(
			"Attempting to establish transmitter session with invalid systemid");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindReceiver();
			breq.setSystemId("banana");
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			breq.setAddressRange(smppAddressRange);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			throw new BindReceiverException(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
		}
		// Response object is null!
		assertEquals(
			"BindReceiver authentication should have failed: response was not ESME_RINVSYSID",
			Data.ESME_RINVSYSID,
			resp.getCommandStatus());
		txBound = true;
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}

	/**
	 *
	 * Condition: Bind with incorrect password
	 * Expected: Operation returns response with cmd_status=ESME_RINVPASWD = 0x0E
	 */

	public void test014BindReceiverInvalidPassword()
		throws BindReceiverException {
		logger.info(
			"Attempting to establish receiver session with invalid password");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindReceiver();
			breq.setSystemId(smppAccountName);
			breq.setPassword("banana");
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			breq.setAddressRange(smppAddressRange);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind receiver. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind receiver. "
					+ e.getMessage());
			throw new BindReceiverException(
				"Exception whilst setting up or executing bind receiver. "
					+ e.getMessage());
		}
		// Response object is null!
		assertEquals(
			"BindReceiver authentication should have failed: response was not ESME_RINVPASWD",
			Data.ESME_RINVPASWD,
			resp.getCommandStatus());
		rxBound = true;
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for RX session. " + e.getMessage());
		}
	}

	public void test021BindTransceiver() throws BindTransceiverException {
		logger.info("Attempting to establish Transceiver session");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransciever();
			breq.setSystemId(smppAccountName);
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			breq.setAddressRange(smppAddressRange);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind Transceiver. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind Transceiver. "
					+ e.getMessage());
			throw new BindTransceiverException(
				"Exception whilst setting up or executing bind Transceiver. "
					+ e.getMessage());
		}
		assertEquals(
			"BindTransceiver failed: response was not ESME_ROK",
			Data.ESME_ROK,
			resp.getCommandStatus());
		logger.info("Established Transceiver session successfully");
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TXRX session. " + e.getMessage());
		}
	}

	/**
	 *
	 * Condition: Bind with incorrect systemid
	 * Expected: Operation returns response with cmd_status=ESME_RINVSYSID = 0x0F
	 */

	public void test023BindTransceiverInvalidSystemID()
		throws BindTransceiverException {
		logger.info(
			"Attempting to establish tranceiver session with invalid systemid");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransciever();
			breq.setSystemId("banana");
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			breq.setAddressRange(smppAddressRange);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
			throw new BindTransceiverException(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
		}
		// Response object is null!
		assertEquals(
			"BindTranceiver authentication should have failed: response was not ESME_RINVSYSID",
			Data.ESME_RINVSYSID,
			resp.getCommandStatus());
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TXRX session. " + e.getMessage());
		}
	}

	/**
	 *
	 * Condition: Bind with incorrect password
	 * Expected: Operation returns response with cmd_status=ESME_RINVPASWD = 0x0E
	 */

	public void test034BindTransceiverInvalidPassword()
		throws BindTransceiverException {
		logger.info(
			"Attempting to establish transceiver session with invalid password");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransciever();
			breq.setSystemId(smppAccountName);
			breq.setPassword("banana");
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			breq.setAddressRange(smppAddressRange);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
			throw new BindTransceiverException(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
		}
		// Response object is null!
		assertEquals(
			"BindTransceiver authentication should have failed: response was not ESME_RINVPASWD",
			Data.ESME_RINVPASWD,
			resp.getCommandStatus());
		rxBound = true;
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TXRX session. " + e.getMessage());
		}
	}
	
	
	public void test001SubmitSM()
			throws SubmitSmFailedException, BindTransmitterException, SocketException {
			logger1.info("Attempting to establish transmitter session");
			Response resp = null;
			Connection conn;
			// get a transmitter session
			try {
				conn = new TCPIPConnection(smppHost, smppPort);
				session = new Session(conn);
				BindRequest breq = new BindTransmitter();
				breq.setSystemId(smppAccountName);
				breq.setPassword(smppPassword);
				breq.setInterfaceVersion((byte) 0x34);
				breq.setSystemType(smppSystemType);
				resp = session.bind(breq);
			} catch (Exception e) {
				logger1.error(
					"Exception whilst setting up or executing bind transmitter. "
						+ e.getMessage());
				fail(
					"Exception whilst setting up or executing bind transmitter. "
						+ e.getMessage());
				throw new BindTransmitterException(
					"Exception whilst setting up or executing bind transmitter. "
						+ e.getMessage());
			}
			assertEquals(
				"BindTransmitter failed: response was not ESME_ROK",
				Data.ESME_ROK,
				resp.getCommandStatus());
			logger1.info("Established transmitter session successfully");

			// now send a message
			try {
				SubmitSM request = new SubmitSM();
				SubmitSMResp response;
				// set values
				request.setServiceType(smppServiceType);
				request.setSourceAddr(srcAddress);
				request.setDestAddr(destAddress);
				request.setShortMessage("SUBMIT_SM test using JUnit");
				// send the request

				request.assignSequenceNumber(true);
				response = session.submit(request);
				logger1.info("Message submitted....");
				assertEquals(
					"SUBMIT_SM failed: response was not ESME_ROK",
					Data.ESME_ROK,
					response.getCommandStatus());
			} catch (SocketException se) {
				logger1.error("Connection has dropped");
				throw se;
			} catch (Exception e) {
				logger1.error(e.getMessage());
				throw new SubmitSmFailedException();
			}

			// Now unbind and disconnect ready for the next test
			try {
				UnbindResp response = session.unbind();
				logger1.info("Unbound...");
			} catch (Exception e) {
				logger1.error(
					"Unbind operation failed for TX session. " + e.getMessage());
			}
		}

		public void test003SubmitSMRequestDeliveryReceipt()
			throws
				SubmitSmFailedException,
				BindTransmitterException,
				BindReceiverException,
				SocketException {
			logger1.info("Attempting to establish transceiver session");
			Response resp = null;
			Connection conn;
			Session session;
			// get a transceiver session
			try {
				conn = new TCPIPConnection(smppHost, smppPort);
				session = new Session(conn);
				BindRequest breq = new BindTransciever();
				breq.setSystemId(smppAccountName);
				breq.setPassword(smppPassword);
				breq.setInterfaceVersion((byte) 0x34);
				breq.setSystemType(smppSystemType);
				resp = session.bind(breq);
			} catch (Exception e) {
				logger1.error(
					"Exception whilst setting up or executing bind transceiver. "
						+ e.getMessage());
				fail(
					"Exception whilst setting up or executing bind transceiver. "
						+ e.getMessage());
				throw new BindTransmitterException(
					"Exception whilst setting up or executing bind transceiver. "
						+ e.getMessage());
			}
			assertEquals(
				"BindTransmitter failed: response was not ESME_ROK",
				Data.ESME_ROK,
				resp.getCommandStatus());
			logger1.info("Established transceiver session successfully");

			// now send a message, requesting a delivery receipt
			try {
				SubmitSM request = new SubmitSM();
				SubmitSMResp response;
				// set values
				request.setServiceType(smppServiceType);
				request.setSourceAddr(srcAddress);
				request.setDestAddr(destAddress);
				request.setShortMessage("SUBMIT_SM test using JUnit");
				request.setRegisteredDelivery((byte) 1);
				//			request.setDataCoding((byte) 0);

				// send the request

				request.assignSequenceNumber(true);
				response = session.submit(request);
				assertEquals(
					"SUBMIT_SM failed: response was not ESME_ROK",
					Data.ESME_ROK,
					response.getCommandStatus());
			} catch (SocketException se) {
				logger1.error("Connection has dropped");
				throw se;
			} catch (Exception e) {
				logger1.error(e.getMessage());
				throw new SubmitSmFailedException();
			}

			// Now wait for the delivery receipt

			// now wait for a message
			PDU pdu = null;
			DeliverSM deliversm;
			Response response;
			boolean gotDeliverSM = false;
			while (!gotDeliverSM) {
				try {
					// wait for a PDU
					pdu = session.receive();
					if (pdu != null) {
						if (pdu instanceof DeliverSM) {
							deliversm = (DeliverSM) pdu;
							gotDeliverSM = true;
							logger1.info("Received DELIVER_SM");
							response = ((Request) pdu).getResponse();
							session.respond(response);

						} else {
							if (pdu instanceof EnquireLinkResp) {
								logger1.debug("EnquireLinkResp received");
							} else {
								logger1.error(
									"Unexpected PDU of type: "
										+ pdu.getClass().getName()
										+ " received - discarding");
								fail(
									"Unexpected PDU type received"
										+ pdu.getClass().getName());
							}
						}
					}
				} catch (SocketException e) {
					fail("Connection has dropped for some reason");
				} catch (IOException ioe) {
					fail("IOException: " + ioe.getMessage());
				} catch (NotSynchronousException nse) {
					fail("NotSynchronousException: " + nse.getMessage());
				} catch (PDUException pdue) {
					fail("PDUException: " + pdue.getMessage());
				} catch (TimeoutException toe) {
					fail("TimeoutException: " + toe.getMessage());
				} catch (WrongSessionStateException wsse) {
					fail("WrongSessionStateException: " + wsse.getMessage());
				}
			}

			// Now unbind and disconnect ready for the next test
			try {
				session.unbind();
			} catch (Exception e) {
				logger1.error(
					"Unbind operation failed for TX session. " + e.getMessage());
			}
		}

		/*
		 * Condition: submit_sm without a source address
		 * Expected: No exceptions. ESME_ROK response.
		 */

		public void test004SubmitSmNoSourceAddress()
			throws SubmitSmFailedException, BindTransmitterException, SocketException {
			Response resp = null;
			Connection conn;
			// get a transmitter session
			try {
				conn = new TCPIPConnection(smppHost, smppPort);
				session = new Session(conn);
				BindRequest breq = new BindTransmitter();
				breq.setSystemId(smppAccountName);
				breq.setPassword(smppPassword);
				breq.setInterfaceVersion((byte) 0x34);
				breq.setSystemType(smppSystemType);
				resp = session.bind(breq);
			} catch (Exception e) {
				logger1.error(
					"Exception whilst setting up or executing bind transmitter. "
						+ e.getMessage());
				fail(
					"Exception whilst setting up or executing bind transmitter. "
						+ e.getMessage());
				throw new BindTransmitterException(
					"Exception whilst setting up or executing bind transmitter. "
						+ e.getMessage());
			}
			assertEquals(
				"BindTransmitter failed: response was not ESME_ROK",
				Data.ESME_ROK,
				resp.getCommandStatus());
			logger1.info("Established transmitter session successfully");

			// now send a message
			try {
				SubmitSM request = new SubmitSM();
				SubmitSMResp response;
				// set values
				request.setServiceType(smppServiceType);
				request.setSourceAddr("");
				request.setDestAddr(destAddress);
				request.setShortMessage(
					"SUBMIT_SM test using JUnit. No source address.");
				// send the request
				request.assignSequenceNumber(true);
				response = session.submit(request);
				logger1.info("Message submitted....");
				assertEquals(
					"SUBMIT_SM failed: response was not ESME_ROK",
					Data.ESME_ROK,
					response.getCommandStatus());
			} catch (SocketException se) {
				logger1.error("Connection has dropped");
				throw se;
			} catch (Exception e) {
				logger1.error(e.getMessage());
				throw new SubmitSmFailedException();
			}
			// Now unbind and disconnect ready for the next test
			try {
				session.unbind();
			} catch (Exception e) {
				logger1.error(
					"Unbind operation failed for TX session. " + e.getMessage());
			}
		}

		/*
		 * Condition: submit_sm without a destination address
		 * Expected: No exceptions. ESME_RINVDSTADR response.
		 */


		public void test006SubmitSMwithOptionalParameters()
			throws SubmitSmFailedException, BindTransmitterException, SocketException {
			Response resp = null;
			Connection conn;
			// get a transmitter session
			try {
				conn = new TCPIPConnection(smppHost, smppPort);
				session = new Session(conn);
				BindRequest breq = new BindTransmitter();
				breq.setSystemId(smppAccountName);
				breq.setPassword(smppPassword);
				breq.setInterfaceVersion((byte) 0x34);
				breq.setSystemType(smppSystemType);
				resp = session.bind(breq);
			} catch (Exception e) {
				logger1.error(
					"Exception whilst setting up or executing bind transmitter. "
						+ e.getMessage());
				fail(
					"Exception whilst setting up or executing bind transmitter. "
						+ e.getMessage());
				throw new BindTransmitterException(
					"Exception whilst setting up or executing bind transmitter. "
						+ e.getMessage());
			}
			assertEquals(
				"BindTransmitter failed: response was not ESME_ROK",
				Data.ESME_ROK,
				resp.getCommandStatus());
			logger1.info("Established transmitter session successfully");

			// now send a message
			try {
				SubmitSM request = new SubmitSM();
				SubmitSMResp response;
				// set values
				byte [] bb = new byte [6];
				bb[0] = (byte) 0x01;
				bb[1] = (byte) 0x02;
				bb[2] = (byte) 0x03;
				bb[3] = (byte) 0x04;
				bb[4] = (byte) 0x05;
				bb[5] = (byte) 0x06;
				request.setCallbackNum(new ByteBuffer(bb));

				bb[5] = (byte) 0x01;
				bb[4] = (byte) 0x02;
				bb[3] = (byte) 0x03;
				bb[2] = (byte) 0x04;
				bb[1] = (byte) 0x05;
				bb[0] = (byte) 0x06;
				request.setCallbackNumAtag(new ByteBuffer(bb));
				
				request.setCallbackNumPresInd((byte) 0x01);
				request.setDataCoding((byte) 3); //ISO-8859-1
				request.setDestAddr(destAddress);
				request.setDestAddrSubunit((byte) 1); // MS display
				request.setDestinationPort((short) 1234);
				byte [] sub = new byte[10];
				sub[0] = (byte) 128;
				sub[1] = 0x31;
				sub[2] = 0x32;
				sub[3] = 0x33;
				sub[4] = 0x32;
				sub[5] = 0x31;
				sub[6] = 0x32;
				sub[7] = 0x33;
				sub[8] = 0x32;
				sub[9] = 0x31;
				request.setDestSubaddress(new ByteBuffer(sub));
				request.setDisplayTime((byte) 1);
				request.setEsmClass((byte) 128); // reply path set
				request.setItsReplyType((byte) 2);
				request.setItsSessionInfo((short)0x0101);
				request.setLanguageIndicator((byte) 1); // english
				// request.setMessagePayload();
				request.setMoreMsgsToSend((byte) 1);
				request.setMsMsgWaitFacilities((byte) 129); // fax waiting
				request.setMsValidity((byte) 3);
				request.setNumberOfMessages((byte) 99);
				// request.setPayloadType();
				request.setPriorityFlag((byte) 3);
				request.setPrivacyIndicator((byte) 3);
				request.setProtocolId((byte) 31);
				request.setReplaceIfPresentFlag((byte) 1);
				request.setSarMsgRefNum((short) 2);
				request.setSarSegmentSeqnum((short) 3);
				request.setSarTotalSegments((short) 4);
				request.setScheduleDeliveryTime("050525161013000+");
				request.setServiceType(smppServiceType);
				request.setShortMessage("SUBMIT_SM test including optional parameters");		
				request.setSmDefaultMsgId((byte)0);
				request.setSmsSignal((short) 10);
				request.setSourceAddr(srcAddress);
				request.setSourceAddrSubunit((byte) 1);
				request.setSourcePort((short) 15);
				sub[0] = (byte) 128;
				sub[1] = 0x33;
				sub[2] = 0x32;
				sub[3] = 0x31;
				sub[4] = 0x32;
				sub[5] = 0x33;
				sub[6] = 0x32;
				sub[7] = 0x31;
				sub[8] = 0x32;
				sub[9] = 0x33;
				request.setSourceSubaddress(new ByteBuffer(sub));
				request.setUserMessageReference((short) 16);
				request.setUserResponseCode((byte) 255);
				request.setUssdServiceOp((byte) 16);
				request.setValidityPeriod("050530161013000+");

				// send the request
				request.assignSequenceNumber(true);
				response = session.submit(request);
				logger1.info("Message submitted....");
				assertEquals(
					"SUBMIT_SM failed: response was not ESME_ROK",
					Data.ESME_ROK,
					response.getCommandStatus());
			} catch (SocketException se) {
				logger1.error("Connection has dropped");
				throw se;
			} catch (Exception e) {
				logger1.error(e.getMessage());
				e.printStackTrace();
				throw new SubmitSmFailedException();
			}

			// Now unbind and disconnect ready for the next test
			try {
				UnbindResp response = session.unbind();
				logger1.info("Unbound...");
			} catch (Exception e) {
				logger1.error(
					"Unbind operation failed for TX session. " + e.getMessage());
			}
		}
}

