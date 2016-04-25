package com.drnushooz.parkinglot;

import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by abhinav on 4/23/16.
 */
public class Parker implements Callable<Boolean>
{
	private final SocketChannel inputSocketChannel;
	private final ConcurrentHashMap<String, Vehicle> carSlots;
	private final ConcurrentHashMap<String, Vehicle> bikeSlots;
	private final Vehicle vehicle;

	public Parker(SocketChannel inputSocketChannel, ConcurrentHashMap<String, Vehicle> carSlots, ConcurrentHashMap<String, Vehicle> bikeSlots, Vehicle vehicle)
	{
		this.inputSocketChannel = inputSocketChannel;
		this.carSlots = carSlots;
		this.bikeSlots = bikeSlots;
		this.vehicle = vehicle;
	}

	public Boolean call()
	{
		String licPlate = vehicle.getLicensePlate();
		if (carSlots.containsKey(licPlate) || bikeSlots.containsKey(licPlate))
		{
			Utils.sendMessageAndClose(inputSocketChannel, String.format("Vehicle %s already exists", licPlate));
			return false;
		} else if (vehicle instanceof Car)
		{
			if (carSlots.size() < ServerEntry.MAX_CAR_SLOTS)
			{
				carSlots.put(licPlate, vehicle);
				Utils.sendMessageAndClose(inputSocketChannel, String.format("Parked car %s", licPlate));
				return true;
			} else
			{
				Utils.sendMessageAndClose(inputSocketChannel, "Parking lot is at capacity");
				return false;
			}
		} else if (vehicle instanceof Bike)
		{
			//Check if there is space in bike slot. If not, add to a car slot
			if (bikeSlots.size() < ServerEntry.MAX_BIKE_SLOTS)
			{
				bikeSlots.put(vehicle.getLicensePlate(), vehicle);
				Utils.sendMessageAndClose(inputSocketChannel, String.format("Parked bike %s", licPlate));
				return true;
			} else if (carSlots.size() < ServerEntry.MAX_CAR_SLOTS)
			{
				carSlots.put(vehicle.getLicensePlate(), vehicle);
				Utils.sendMessageAndClose(inputSocketChannel, String.format("Parked bike %s", licPlate));
				return true;
			} else
			{
				Utils.sendMessageAndClose(inputSocketChannel, "There is no space for bike");
				return false;
			}
		} else
		{
			Utils.sendMessageAndClose(inputSocketChannel, "Invalid vehicle type");
			return false;
		}
	}
}
