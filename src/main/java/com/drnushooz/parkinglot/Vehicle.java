package com.drnushooz.parkinglot;

/**
 * Created by abhinav on 4/23/16.
 */
public class Vehicle
{
	private final String licensePlate;

	public Vehicle(String license)
	{
		licensePlate = license;
	}

	public String getLicensePlate()
	{
		return licensePlate;
	}
}
