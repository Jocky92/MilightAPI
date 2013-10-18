package de.toman.milight;

import java.awt.Color;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class represents a MiLight WiFi box and is able to send commands to a
 * particular box.
 * 
 * @author Stefan Toman (toman@tum.de)
 */
public class WiFiBox {
	/**
	 * The address of the WiFi box
	 */
	private InetAddress address;

	/**
	 * The port of the WiFi box
	 */
	private int port;

	/**
	 * The default port for unconfigured boxes.
	 */
	public static final int DEFAULT_PORT = 8899;

	/**
	 * The sleep time between both messages for switching lights to the white
	 * mode.
	 */
	public static final int DEFAULT_SLEEP_BETWEEN_MESSAGES = 100;

	/**
	 * The command code for "RGBW COLOR LED ALL OFF".
	 */
	public static final int COMMAND_ALL_OFF = 0x41;

	/**
	 * The command code for "GROUP 1 ALL OFF".
	 */
	public static final int COMMAND_GROUP_1_OFF = 0x46;
	/**
	 * The command code for "GROUP 2 ALL OFF".
	 */
	public static final int COMMAND_GROUP_2_OFF = 0x48;
	/**
	 * The command code for "GROUP 3 ALL OFF".
	 */
	public static final int COMMAND_GROUP_3_OFF = 0x4A;
	/**
	 * The command code for "GROUP 4 ALL OFF".
	 */
	public static final int COMMAND_GROUP_4_OFF = 0x4C;
	/**
	 * The command code for "RGBW COLOR LED ALL ON".
	 */
	public static final int COMMAND_ALL_ON = 0x42;

	/**
	 * The command code for "GROUP 1 ALL ON".
	 */
	public static final int COMMAND_GROUP_1_ON = 0x45;

	/**
	 * The command code for "GROUP 2 ALL ON".
	 */
	public static final int COMMAND_GROUP_2_ON = 0x47;

	/**
	 * The command code for "GROUP 3 ALL ON".
	 */
	public static final int COMMAND_GROUP_3_ON = 0x49;

	/**
	 * The command code for "GROUP 4 ALL ON".
	 */
	public static final int COMMAND_GROUP_4_ON = 0x4B;

	/**
	 * The command code for "SET COLOR TO WHITE (GROUP ALL)". Send an "ON"
	 * command 100ms before.
	 */
	public static final int COMMAND_ALL_WHITE = 0xC2;

	/**
	 * The command code for "SET COLOR TO WHITE (GROUP 1)". Send an "ON" command
	 * 100ms before.
	 */
	public static final int COMMAND_GROUP_1_WHITE = 0xC5;

	/**
	 * The command code for "SET COLOR TO WHITE (GROUP 2)". Send an "ON" command
	 * 100ms before.
	 */
	public static final int COMMAND_GROUP_2_WHITE = 0xC7;

	/**
	 * The command code for "SET COLOR TO WHITE (GROUP 3)". Send an "ON" command
	 * 100ms before.
	 */
	public static final int COMMAND_GROUP_3_WHITE = 0xC9;

	/**
	 * The command code for "SET COLOR TO WHITE (GROUP 4)". Send an "ON" command
	 * 100ms before.
	 */
	public static final int COMMAND_GROUP_4_WHITE = 0xCB;

	/**
	 * The command code for "DISCO MODE".
	 */
	public static final int COMMAND_DISCO = 0x4D;

	/**
	 * The command code for "DISCO SPEED FASTER".
	 */
	public static final int COMMAND_DISCO_FASTER = 0x44;

	/**
	 * The command code for "DISCO SPEED SLOWER".
	 */
	public static final int COMMAND_DISCO_SLOWER = 0x43;

	/**
	 * The command code for "COLOR SETTING" (part of a two-byte command).
	 */

	public static final int COMMAND_COLOR = 0x40;
	/**
	 * The command code for "DIRECT BRIGHTNESS SETTING" (part of a two-byte
	 * command).
	 */
	public static final int COMMAND_BRIGHTNESS = 0x4E;

	/**
	 * A constructor creating a new instance of the WiFi box class.
	 * 
	 * @param address
	 *            is the address of the WiFi box
	 * @param port
	 *            is the port of the WiFi box (omit this if unsure)
	 */
	public WiFiBox(InetAddress address, int port) {
		super();
		this.address = address;
		this.port = port;
	}

	/**
	 * A constructor creating a new instance of the WiFi box class using the
	 * default port number.
	 * 
	 * @param address
	 *            is the address of the WiFi box
	 */
	public WiFiBox(InetAddress address) {
		this(address, DEFAULT_PORT);
	}

	/**
	 * A constructor creating a new instance of the WiFi box class. The address
	 * is resolved from a hostname or ip address.
	 * 
	 * @param host
	 *            is the host given as hostname such as "domain.tld" or string
	 *            repesentation of an ip address
	 * @param port
	 *            is the port of the WiFi box (omit this if unsure)
	 * @throws UnknownHostException
	 *             if the hostname could not be resolved
	 */
	public WiFiBox(String host, int port) throws UnknownHostException {
		this(InetAddress.getByName(host), port);
	}

	/**
	 * A constructor creating a new instance of the WiFi box class using the
	 * default port number. The address is resolved from a hostname or ip
	 * address.
	 * 
	 * @param host
	 *            is the host given as hostname such as "domain.tld" or string
	 *            repesentation of an ip address
	 * @throws UnknownHostException
	 *             if the hostname could not be resolved
	 */
	public WiFiBox(String host) throws UnknownHostException {
		this(host, DEFAULT_PORT);
	}

	/**
	 * Get the group of lights that is controlled by a given group number. The
	 * Lights instance may be used to control the groups of lights individually
	 * and mix different WiFi boxes.
	 * 
	 * @param group
	 *            is the number of the group at the WiFi box (between 1 and 4)
	 * @return the group of lights that is controled by the given group number
	 * @throws IllegalArgumentException
	 *             if the group number is not between 1 and 4
	 */
	public Lights getLights(int group) throws IllegalArgumentException {
		// check group number
		if (1 > group || group > 4) {
			throw new IllegalArgumentException(
					"The group number must be between 1 and 4");
		}

		// create new instance
		return new Lights(this, group);
	}

	/**
	 * This function sends an array of bytes to the WiFi box. The bytes should
	 * be a valid command, i.e. the array's length should be three.
	 * 
	 * @param messages
	 *            is an array of message codes to send
	 * @throws IllegalArgumentException
	 *             if the length of the array is not 3
	 * @throws IOException
	 *             if the message could not be sent
	 */
	private void sendMessage(byte[] messages) throws IOException {
		if (messages.length != 3) {
			throw new IllegalArgumentException(
					"The message to send should consist of exactly 3 bytes.");
		}

		DatagramSocket socket = new DatagramSocket();
		DatagramPacket packet = new DatagramPacket(messages, messages.length,
				address, port);
		socket.send(packet);
		socket.close();
	}

	/**
	 * This function pads a one-byte message to a three-byte message by adding
	 * the default bytes 0x00 0x55.
	 * 
	 * @param message
	 *            is the message to pad
	 * @return is the padded message
	 */
	private byte[] padMessage(int message) {
		byte[] paddedMessage = { (byte) message, 0x55 & 0x00, 0x55 & 0x55 };
		return paddedMessage;
	}

	/**
	 * This function pads a two-byte message to a three-byte message by adding
	 * the default byte 0x55.
	 * 
	 * @param message1
	 *            is the first byte of the message to pad
	 * @param message2
	 *            is the second byte of the message to pad
	 * @return is the padded message
	 */
	private byte[] padMessage(int message1, int message2) {
		byte[] paddedMessage = { (byte) message1, (byte) message2, 0x55 & 0x55 };
		return paddedMessage;
	}

	/**
	 * This function constructs a three-byte command to switch on a given group
	 * of lights. This array is ready to be sent to the WiFi box.
	 * 
	 * @param group
	 *            is the group of lights to switch on
	 * @throws IllegalArgumentException
	 *             if the group number is not between 1 and 4
	 * @return the message array to send to the WiFi box
	 */
	private byte[] getSwitchOnCommand(int group)
			throws IllegalArgumentException {
		switch (group) {
		case 1:
			return padMessage(COMMAND_GROUP_1_ON);
		case 2:
			return padMessage(COMMAND_GROUP_2_ON);
		case 3:
			return padMessage(COMMAND_GROUP_3_ON);
		case 4:
			return padMessage(COMMAND_GROUP_4_ON);
		default:
			throw new IllegalArgumentException(
					"The group number must be between 1 and 4");
		}
	}

	/**
	 * This function constructs a three-byte command to switch off a given group
	 * of lights. This array is ready to be sent to the WiFi box.
	 * 
	 * @param group
	 *            is the group of lights to switch off
	 * @throws IllegalArgumentException
	 *             if the group number is not between 1 and 4
	 * @return the message array to send to the WiFi box
	 */
	private byte[] getSwitchOffCommand(int group)
			throws IllegalArgumentException {
		switch (group) {
		case 1:
			return padMessage(COMMAND_GROUP_1_OFF);
		case 2:
			return padMessage(COMMAND_GROUP_2_OFF);
		case 3:
			return padMessage(COMMAND_GROUP_3_OFF);
		case 4:
			return padMessage(COMMAND_GROUP_4_OFF);
		default:
			throw new IllegalArgumentException(
					"The group number must be between 1 and 4");
		}
	}

	/**
	 * This function constructs a three-byte command to switch a given group of
	 * lights to the white mode. This array is ready to be sent to the WiFi box.
	 * 
	 * @param group
	 *            is the group of lights to switch to the white mode
	 * @throws IllegalArgumentException
	 *             if the group number is not between 1 and 4
	 * @return the message array to send to the WiFi box
	 */
	private byte[] getWhiteModeCommand(int group)
			throws IllegalArgumentException {
		switch (group) {
		case 1:
			return padMessage(COMMAND_GROUP_1_WHITE);
		case 2:
			return padMessage(COMMAND_GROUP_2_WHITE);
		case 3:
			return padMessage(COMMAND_GROUP_3_WHITE);
		case 4:
			return padMessage(COMMAND_GROUP_4_WHITE);
		default:
			throw new IllegalArgumentException(
					"The group number must be between 1 and 4");
		}
	}

	/**
	 * This function sends an one-byte control message to the WiFi box. The
	 * message is padded with 0x00 0x55 as given in the documentation.
	 * 
	 * @param message
	 *            is the message code to send
	 * @throws IOException
	 *             if the message could not be sent
	 */
	private void sendMessage(int message) throws IOException {
		// pad the message with 0x00 0x55
		byte[] paddedMessage = padMessage(message);

		// send the padded message
		sendMessage(paddedMessage);
	}

	/**
	 * This function sends a two-byte control message to the WiFi box. The
	 * message is padded with 0x55 as given in the documentation.
	 * 
	 * @param message1
	 *            is the first byte of the message to send
	 * @param message2
	 *            is the second byte of the message to send
	 * @throws IOException
	 *             if the message could not be sent
	 */
	private void sendMessage(int message1, int message2) throws IOException {
		// pad the message with 0x55
		byte[] paddedMessage = padMessage(message1, message2);

		// send the padded message
		sendMessage(paddedMessage);
	}

	/**
	 * This function sends multiple three-byte messages to the WiFi box. All
	 * elements of the message array should be byte arrays with three elements.
	 * Note that the messages are sent in a new thread. Therefore, you should
	 * not send other commands directly after executing this one. Also, there
	 * are no exceptions when sending messages fails since they occur in another
	 * thread.
	 * 
	 * @param messages
	 *            is the messages to send (in order)
	 * @param sleep
	 *            is the time to wait between two message in milliseconds
	 * @throws IllegalArgumentException
	 *             if some of the messages in the array don't consist of exactly
	 *             three bytes
	 */
	private void sendMultipleMessages(final byte[][] messages, final long sleep)
			throws IllegalArgumentException {
		// check arguments
		for (int i = 0; i < messages.length; i++) {
			if (messages[i].length != 3) {
				throw new IllegalArgumentException(
						"All messages should consist of three bytes.");
			}
		}

		// start new thread
		new Thread(new Runnable() {
			public void run() {
				try {
					for (byte[] message : messages) {
						WiFiBox.this.sendMessage(message);
						Thread.sleep(sleep);
					}
				} catch (IOException e) {
					// if the message could not be sent
				} catch (InterruptedException e) {
					// if the thread could not sleep
				}
			}
		}).start();
	}

	/**
	 * This function sends multiple one-byte messages to the WiFi box. All of
	 * the are padded with the corresponding bytes. Note that the messages are
	 * sent in a new thread. Therefore, you should not send other commands
	 * directly after executing this one. Also, there are no exceptions when
	 * sending messages fails since they occur in another thread.
	 * 
	 * @param messages
	 *            is the messages to send (in order)
	 * @param sleep
	 *            is the time to wait between two message in milliseconds
	 */
	private void sendMultipleMessages(final int[] messages, final long sleep) {
		// pad messages
		byte[][] paddedMessages = new byte[messages.length][3];
		for (int i = 0; i < messages.length; i++) {
			paddedMessages[i] = padMessage(messages[i]);
		}

		// send the padded messages
		sendMultipleMessages(paddedMessages, sleep);
	}

	/**
	 * Switch all lights off (all groups).
	 * 
	 * @throws IOException
	 *             if the message could not be sent
	 */
	public void off() throws IOException {
		sendMessage(COMMAND_ALL_OFF);
	}

	/**
	 * Switch all lights of a particular group off.
	 * 
	 * @param group
	 *            the group to switch of (between 1 and 4)
	 * @throws IOException
	 *             if the message could not be sent
	 * @throws IllegalArgumentException
	 *             if the group number is not between 1 and 4
	 */
	public void off(int group) throws IOException, IllegalArgumentException {
		sendMessage(getSwitchOffCommand(group));
	}

	/**
	 * Switch all lights on (all groups).
	 * 
	 * @throws IOException
	 *             if the message could not be sent
	 */
	public void on() throws IOException {
		sendMessage(COMMAND_ALL_ON);
	}

	/**
	 * Switch all lights of a particular group on.
	 * 
	 * @param group
	 *            the group to switch of (between 1 and 4)
	 * @throws IOException
	 *             if the message could not be sent
	 * @throws IllegalArgumentException
	 *             if the group number is not between 1 and 4
	 */
	public void on(int group) throws IOException, IllegalArgumentException {
		sendMessage(getSwitchOnCommand(group));
	}

	/**
	 * Switch all lights in all groups to the white mode. Note that the messages
	 * are sent in a new thread. Therefore, you should not send other commands
	 * directly after executing this one. Also, there are no exceptions when
	 * sending messages fails since they occur in another thread.
	 */
	public void white() {
		int[] messages = { COMMAND_ALL_ON, COMMAND_ALL_WHITE };
		sendMultipleMessages(messages, DEFAULT_SLEEP_BETWEEN_MESSAGES);
	}

	/**
	 * Switch all lights in a particular group to the white mode. Note that the
	 * messages are sent in a new thread. Therefore, you should not send other
	 * commands directly after executing this one. Also, there are no exceptions
	 * when sending messages fails since they occur in another thread.
	 * 
	 * @param group
	 *            the group to switch of (between 1 and 4)
	 * @throws IllegalArgumentException
	 *             if the group number is not between 1 and 4
	 */
	public void white(int group) throws IllegalArgumentException {
		// create message array
		byte[][] messages = new byte[2][3];

		// switch on first
		messages[0] = getSwitchOnCommand(group);

		// switch to white mode
		messages[1] = getWhiteModeCommand(group);

		// send messages
		sendMultipleMessages(messages, DEFAULT_SLEEP_BETWEEN_MESSAGES);
	}

	/**
	 * Trigger the disco mode for the active group of lights (the last one that
	 * was switched on).
	 * 
	 * @throws IOException
	 *             if the message could not be sent
	 */
	public void discoMode() throws IOException {
		sendMessage(COMMAND_DISCO);
	}

	/**
	 * Triggers the disco mode for a particular group of lights. The lights will
	 * be switched on before to activate them.Note that the messages are sent in
	 * a new thread. Therefore, you should not send other commands directly
	 * after executing this one. Also, there are no exceptions when sending
	 * messages fails since they occur in another thread.
	 * 
	 * @param group
	 *            the group to switch of (between 1 and 4)
	 * @throws IllegalArgumentException
	 *             if the group number is not between 1 and 4
	 */
	public void discoMode(int group) throws IllegalArgumentException {
		// create message array
		byte[][] messages = new byte[2][3];

		// switch on first
		messages[0] = getSwitchOnCommand(group);

		// start disco mode
		messages[1] = padMessage(COMMAND_DISCO);

		// send messages
		sendMultipleMessages(messages, DEFAULT_SLEEP_BETWEEN_MESSAGES);
	}

	/**
	 * Increase the disco mode's speed for the active group of lights (the last
	 * one that was switched on).
	 * 
	 * @throws IOException
	 *             if the message could not be sent
	 */
	public void discoModeFaster() throws IOException {
		sendMessage(COMMAND_DISCO_FASTER);
	}

	/**
	 * Decrease the disco mode's speed for the active group of lights (the last
	 * one that was switched on).
	 * 
	 * @throws IOException
	 *             if the message could not be sent
	 */
	public void discoModeSlower() throws IOException {
		sendMessage(COMMAND_DISCO_SLOWER);
	}

	/**
	 * Set the brightness value for the currently active group of lights (the
	 * last one that was switched on).
	 * 
	 * @param value
	 *            is the brightness value to set (between
	 *            MilightColor.MIN_BRIGHTNESS and MilightColor.MAX_BRIGHTNESS)
	 * @throws IOException
	 *             if the message could not be sent
	 * @throws IllegalArgumentException
	 *             if the brightness value is not between
	 *             MilightColor.MIN_BRIGHTNESS and MilightColor.MAX_BRIGHTNESS
	 */
	public void brightness(int value) throws IOException,
			IllegalArgumentException {
		// check argument
		if (value < MilightColor.MIN_BRIGHTNESS
				|| value > MilightColor.MAX_BRIGHTNESS) {
			throw new IllegalArgumentException(
					"The brightness value should be between MilightColor.MIN_BRIGHTNESS and MilightColor.MAX_BRIGHTNESS");
		}

		// send message to the WiFi box
		sendMessage(COMMAND_BRIGHTNESS, value);
	}

	/**
	 * Set the brightness value for a given group of lights.
	 * 
	 * @param group
	 *            is the number of the group to set the brightness for
	 * @param value
	 *            is the brightness value to set (between
	 *            MilightColor.MIN_BRIGHTNESS and MilightColor.MAX_BRIGHTNESS)
	 * @throws IOException
	 *             if the message could not be sent
	 * @throws IllegalArgumentException
	 *             if group is not between 1 and 4 or the brightness value is
	 *             not between MilightColor.MIN_BRIGHTNESS and
	 *             MilightColor.MAX_BRIGHTNESS
	 */
	public void brightness(int group, int value) throws IOException,
			IllegalArgumentException {
		// check arguments
		if (value < MilightColor.MIN_BRIGHTNESS
				|| value > MilightColor.MAX_BRIGHTNESS) {
			throw new IllegalArgumentException(
					"The brightness value should be between MilightColor.MIN_BRIGHTNESS and MilightColor.MAX_BRIGHTNESS");
		}

		// create message array
		byte[][] messages = new byte[2][3];

		// switch on first
		messages[0] = getSwitchOnCommand(group);

		// adjust brightness
		messages[1] = padMessage(COMMAND_BRIGHTNESS, value);

		// send messages
		sendMultipleMessages(messages, DEFAULT_SLEEP_BETWEEN_MESSAGES);
	}

	/**
	 * Set the color value for the currently active group of lights (the last
	 * one that was switched on).
	 * 
	 * @param value
	 *            is the color value to set (between MilightColor.MIN_COLOR and
	 *            MilightColor.MAX_COLOR)
	 * @throws IOException
	 *             if the message could not be sent
	 * @throws IllegalArgumentException
	 *             if the color value is not between MilightColor.MIN_COLOR and
	 *             MilightColor.MAX_COLOR
	 */
	public void color(int value) throws IOException, IllegalArgumentException {
		// check argument
		if (value < MilightColor.MIN_COLOR || value > MilightColor.MAX_COLOR) {
			throw new IllegalArgumentException(
					"The color value should be between MilightColor.MIN_COLOR and MilightColor.MAX_COLOR");
		}

		// send message to the WiFi box
		sendMessage(COMMAND_COLOR, value);
	}

	/**
	 * Set the color value for the currently active group of lights (the last
	 * one that was switched on).
	 * 
	 * @param color
	 *            is the color to set
	 * @throws IOException
	 *             if the message could not be sent
	 */
	public void color(MilightColor color) throws IOException {
		color(color.getMilightHue());
	}

	/**
	 * Set the color value for the currently active group of lights (the last
	 * one that was switched on).
	 * 
	 * @param color
	 *            is the color to set
	 * @throws IOException
	 *             if the message could not be sent
	 */
	public void color(Color color) throws IOException {
		color(new MilightColor(color));
	}

	/**
	 * Set the color value for a given group of lights.
	 * 
	 * @param group
	 *            is the number of the group to set the color for
	 * @param value
	 *            is the color value to set (between MilightColor.MIN_COLOR and
	 *            MilightColor.MAX_COLOR)
	 * @throws IOException
	 *             if the message could not be sent
	 * @throws IllegalArgumentException
	 *             if group is not between 1 and 4 or the color value is not
	 *             between MilightColor.MIN_COLOR and MilightColor.MAX_COLOR
	 */
	public void color(int group, int value) throws IOException,
			IllegalArgumentException {
		// check arguments
		if (value < MilightColor.MIN_COLOR || value > MilightColor.MAX_COLOR) {
			throw new IllegalArgumentException(
					"The color value should be between MilightColor.MIN_COLOR and MilightColor.MAX_COLOR");
		}

		// create message array
		byte[][] messages = new byte[2][3];

		// switch on first
		messages[0] = getSwitchOnCommand(group);

		// adjust color
		messages[1] = padMessage(COMMAND_COLOR, value);

		// send messages
		sendMultipleMessages(messages, DEFAULT_SLEEP_BETWEEN_MESSAGES);
	}

	/**
	 * Set the color value for a given group of lights.
	 * 
	 * @param group
	 *            is the number of the group to set the color for
	 * @param color
	 *            is the color to set
	 * @throws IOException
	 *             if the message could not be sent
	 * @throws IllegalArgumentException
	 *             if group is not between 1 and 4
	 */
	public void color(int group, MilightColor color) throws IOException,
			IllegalArgumentException {
		color(group, color.getMilightHue());
	}

	/**
	 * Set the color value for a given group of lights.
	 * 
	 * @param group
	 *            is the number of the group to set the color for
	 * @param color
	 *            is the color to set
	 * @throws IOException
	 *             if the message could not be sent
	 * @throws IllegalArgumentException
	 *             if group is not between 1 and 4
	 */
	public void color(int group, Color color) throws IOException,
			IllegalArgumentException {
		color(group, new MilightColor(color));
	}

	/**
	 * Set the color and brightness values for the currently active group of
	 * lights (the last one that was switched on). Both values are extracted
	 * from the color given to the function by transforming it to an HSB color.
	 * 
	 * @param color
	 *            is the color to extract hue and brightness from
	 */
	public void colorAndBrightness(MilightColor color) {
		// create message array
		byte[][] messages = new byte[2][3];

		// adjust color
		messages[0] = padMessage(COMMAND_COLOR, color.getMilightHue());

		// adjust brightness
		messages[1] = padMessage(COMMAND_BRIGHTNESS,
				color.getMilightBrightness());

		// send messages
		sendMultipleMessages(messages, DEFAULT_SLEEP_BETWEEN_MESSAGES);
	}

	/**
	 * Set the color and brightness values for the currently active group of
	 * lights (the last one that was switched on). Both values are extracted
	 * from the color given to the function by transforming it to an HSB color.
	 * 
	 * @param color
	 *            is the color to extract hue and brightness from
	 */
	public void colorAndBrightness(Color color) {
		colorAndBrightness(new MilightColor(color));
	}

	/**
	 * Set the color and brightness values for a given group of lights. Both
	 * values are extracted from the color given to the function by transforming
	 * it to an HSB color.
	 * 
	 * @param group
	 *            is the number of the group to set the color for
	 * @param color
	 *            is the color to extract hue and brightness from
	 * @throws IllegalArgumentException
	 *             if group is not between 1 and 4
	 */
	public void colorAndBrightness(int group, MilightColor color) {
		// create message array
		byte[][] messages = new byte[3][3];

		// switch on first
		messages[0] = getSwitchOnCommand(group);

		// adjust color
		messages[1] = padMessage(COMMAND_COLOR, color.getMilightHue());

		// adjust brightness
		messages[2] = padMessage(COMMAND_BRIGHTNESS,
				color.getMilightBrightness());

		// send messages
		sendMultipleMessages(messages, DEFAULT_SLEEP_BETWEEN_MESSAGES);
	}
}
