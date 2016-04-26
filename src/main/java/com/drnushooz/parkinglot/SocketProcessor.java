package com.drnushooz.parkinglot;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by abhinav on 4/23/16.
 */
public class SocketProcessor implements Runnable
{
	private static final String ERROR_NOT_ENOUGH_OPERANDS = "Not enough operands in input message";
	private static final String ERROR_INVALID_OPERATION = "Invalid operation. Only park and unpark are supported";
	private static final String ERROR_INVALID_VEH_TYPE = "Invalid vehicle type. You can only park a car or bike";
	private static ExecutorService parkUnparkService;
	private static ConcurrentHashMap<String, Vehicle> carSlots, bikeSlots;
	private static int MAX_MESSAGE_LENGTH;
	private SocketChannel inputSockChannel;

	public SocketProcessor(SocketChannel inputSockChannel, ExecutorService parkUnparkService,
						   ConcurrentHashMap<String, Vehicle> carSlots,
						   ConcurrentHashMap<String, Vehicle> bikeSlots,
						   Map<ConfigParams, Integer> serverSettings)
	{
		this.inputSockChannel = inputSockChannel;
		this.parkUnparkService = parkUnparkService;
		this.carSlots = carSlots;
		this.bikeSlots = bikeSlots;
		MAX_MESSAGE_LENGTH = serverSettings.get(ConfigParams.MAX_MESSAGE_LEN_BYTES);
	}

	public void run()
	{
		ByteBuffer inputMessageBuffer = ByteBuffer.allocate(MAX_MESSAGE_LENGTH);
		try
		{
			inputSockChannel.read(inputMessageBuffer);
			String[] messageTokens = Utils.byteBufferToString(inputMessageBuffer).split("\\s+");
			if (messageTokens.length < 1)
			{
				Utils.sendMessageAndClose(inputSockChannel, ERROR_NOT_ENOUGH_OPERANDS);
				return;
			}

			String operation = messageTokens[0];
			//Decide where to send this operation
			if (operation.compareToIgnoreCase("park") == 0)
			{
				if (messageTokens.length < 3)
				{
					Utils.sendMessageAndClose(inputSockChannel, "Not enough operands");
				}
				Vehicle vehicle;
				String vehType = messageTokens[1];
				if (vehType.compareToIgnoreCase("car") == 0)
					vehicle = new Car(messageTokens[2]);
				else if (vehType.compareToIgnoreCase("bike") == 0)
					vehicle = new Bike(messageTokens[2]);
				else
				{
					Utils.sendMessageAndClose(inputSockChannel, ERROR_INVALID_VEH_TYPE);
					return;
				}
				parkUnparkService.submit(new Parker(inputSockChannel, carSlots, bikeSlots, vehicle));
			} else if (operation.compareToIgnoreCase("unpark") == 0)
			{
				if (messageTokens.length < 2)
				{
					Utils.sendMessageAndClose(inputSockChannel, ERROR_NOT_ENOUGH_OPERANDS);
					return;
				}
				parkUnparkService.submit(new Unparker(inputSockChannel, carSlots, bikeSlots, messageTokens[1]));
			} else
			{
				Utils.sendMessageAndClose(inputSockChannel, ERROR_INVALID_OPERATION);
				return;
			}
		} catch (IOException ioe)
		{
			try
			{
				System.out.printf("Error reading from socket %s %s%n", inputSockChannel.getRemoteAddress(), ExceptionUtils.getStackTrace(ioe));
			} catch (IOException ioe2)
			{
				System.out.printf("Exception while getting input socket address in IOE %s%n", ExceptionUtils.getStackTrace(ioe2));
			}
		}
	}
}
