package com.drnushooz.parkinglot;

import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by abhinav on 4/23/16.
 */
public class Unparker implements Callable<Vehicle>
{
	private SocketChannel inputSocketChannel;
	private ConcurrentHashMap<String, Vehicle> carSlots;
	private ConcurrentHashMap<String, Vehicle> bikeSlots;
	private String licensePlate;

	public Unparker(SocketChannel inputSocketChannel, ConcurrentHashMap<String, Vehicle> carSlots, ConcurrentHashMap<String, Vehicle> bikeSlots, String licensePlate)
	{
		this.inputSocketChannel = inputSocketChannel;
		this.carSlots = carSlots;
		this.bikeSlots = bikeSlots;
		this.licensePlate = licensePlate;
	}

	public Vehicle call()
	{
		//If the vehicle is of type car, look for it in carSlots otherwise jump to bike
		Vehicle locatedVeh = carSlots.get(licensePlate);
		if (locatedVeh == null)
			locatedVeh = bikeSlots.get(licensePlate);
		if (locatedVeh == null)
		{
			Utils.sendMessageAndClose(inputSocketChannel, String.format("Vehicle with license plate %s not found", licensePlate));
			return null;
		}
		Utils.sendMessageAndClose(inputSocketChannel, String.format("Vehicle %s removed successfully", locatedVeh.getLicensePlate()));
		return locatedVeh;
	}
}
