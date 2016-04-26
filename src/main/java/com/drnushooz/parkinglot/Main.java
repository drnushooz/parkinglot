package com.drnushooz.parkinglot;

import jdk.nashorn.internal.runtime.regexp.joni.Config;

import java.util.EnumMap;
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
		Map<ConfigParams, Integer> serverProps = new EnumMap<>(ConfigParams.class);
		serverProps.put(ConfigParams.PORT, Integer.valueOf(args[0]));
		serverProps.put(ConfigParams.MAX_CAR_SLOTS, 200);
		serverProps.put(ConfigParams.MAX_BIKE_SLOTS, 200);
		serverProps.put(ConfigParams.MAX_SERVICE_THREADS, 10);
		serverProps.put(ConfigParams.MAX_SOCK_PROC_THREADS, 5);
		serverProps.put(ConfigParams.MAX_MESSAGE_LEN_BYTES, 50);

		ServerEntry serverEntry = new ServerEntry(serverProps);
		Thread serverThread = new Thread(serverEntry);
		serverThread.start();
	}
}
