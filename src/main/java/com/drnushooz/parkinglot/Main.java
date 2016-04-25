package com.drnushooz.parkinglot;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by abhinav on 4/23/16.
 */
public class Main
{
	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.out.println("Provide server port number as first argument");
			System.exit(-1);
		}
		Map<String, Integer> serverProps = new HashMap<>();
		serverProps.put("port", Integer.valueOf(args[0]));
		serverProps.put("maxCarSlots", 200);
		serverProps.put("maxBikeSlots", 200);
		serverProps.put("maxServiceThreads", 10);
		serverProps.put("maxSockProcThreads", 5);
		serverProps.put("maxMessageLenBytes", 50);

		ServerEntry serverEntry = new ServerEntry(serverProps);
		Thread serverThread = new Thread(serverEntry);
		serverThread.start();
	}
}
