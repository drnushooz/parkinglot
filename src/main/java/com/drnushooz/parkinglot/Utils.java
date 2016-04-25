package com.drnushooz.parkinglot;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * Created by abhinav on 4/24/16.
 */
public abstract class Utils
{
	static void sendMessageAndClose(final SocketChannel inputSockChannel, final String message)
	{
		System.out.println(message);
		final ByteBuffer outputError = ByteBuffer.wrap(message.getBytes());
		outputError.flip();
		try
		{
			while (outputError.hasRemaining())
				inputSockChannel.write(outputError);
			inputSockChannel.close();
		} catch (IOException ioe)
		{
			try
			{
				System.out.printf("Exception while sending error message to %s %s%n", inputSockChannel.getRemoteAddress(), ExceptionUtils.getStackTrace(ioe));
			} catch (IOException ioe2)
			{
				System.out.printf("Exception while getting input socket address in IOE %s%n", ExceptionUtils.getStackTrace(ioe2));
			}
		}
	}

	static String byteBufferToString(final ByteBuffer inputMessageBuffer)
	{
		final byte[] messageArr;
		if (inputMessageBuffer.hasArray())
			messageArr = inputMessageBuffer.array();
		else
		{
			messageArr = new byte[inputMessageBuffer.remaining()];
			//Do not affect the original buffer positioning
			inputMessageBuffer.duplicate().get(messageArr);
		}
		return new String(messageArr, StandardCharsets.UTF_8).trim();
	}

}
