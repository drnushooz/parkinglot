package com.drnushooz.parkinglot;

import org.apache.commons.lang3.exception.ExceptionUtils;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by abhinav on 4/23/16.
 */
public class ServerEntry implements Runnable
{
	public static Map<ConfigParams, Integer> serverSettings;
	public static int MAX_CAR_SLOTS, MAX_BIKE_SLOTS;
	private static int PORT;
	private Selector serverSelector;
	private ConcurrentHashMap<String, Vehicle> carSlots;
	private ConcurrentHashMap<String, Vehicle> bikeSlots;
	private ExecutorService parkUnparkService, socketProcessorService;
	private ServerSocketChannel serverSocketChannel;

	public ServerEntry(final Map<ConfigParams, Integer> props)
	{
		serverSettings = props;
		carSlots = new ConcurrentHashMap<>();
		bikeSlots = new ConcurrentHashMap<>();
		PORT = props.get(ConfigParams.PORT);
		MAX_BIKE_SLOTS = props.get(ConfigParams.MAX_BIKE_SLOTS);
		MAX_CAR_SLOTS = props.get(ConfigParams.MAX_CAR_SLOTS);
		socketProcessorService = Executors.newFixedThreadPool(props.get(ConfigParams.MAX_SOCK_PROC_THREADS));
		parkUnparkService = Executors.newFixedThreadPool(props.get(ConfigParams.MAX_SERVICE_THREADS));

		try
		{
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
			serverSocketChannel.configureBlocking(false);
			serverSelector = Selector.open();
			serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
		} catch (IOException ioe)
		{
			System.out.printf("Failed in starting server: %s%n", ExceptionUtils.getStackTrace(ioe));
			return;
		}

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				shutdownServer();
			}
		});
	}

	public void run()
	{
		System.out.println("Starting server main event loop");
		while (true)
		{
			try
			{
				int n = serverSelector.select();
				if (n == 0)
					continue;
				Iterator<SelectionKey> keyIterator = serverSelector.selectedKeys().iterator();
				while (keyIterator.hasNext())
				{
					SelectionKey selectionKey = keyIterator.next();
					SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
					if (socketChannel == null)
						continue;
					socketProcessorService.submit(new SocketProcessor(socketChannel, parkUnparkService, carSlots, bikeSlots, serverSettings));
					keyIterator.remove();
				}
			} catch (IOException ioe)
			{
				System.out.printf("Exception in accepting incoming socket: %s%n", ExceptionUtils.getStackTrace(ioe));
				return;
			}
		}
	}

	private void shutdownServer()
	{
		System.out.println("Shutting down the server");
		try
		{
			if (serverSocketChannel != null && serverSocketChannel.isOpen())
				serverSocketChannel.close();
		} catch (IOException ioe)
		{
			System.out.printf("Exception in shutting down server: %s%n", ioe.getLocalizedMessage());
		}

		try
		{
			socketProcessorService.shutdown();
			parkUnparkService.shutdown();
			socketProcessorService.awaitTermination(1, TimeUnit.MINUTES);
			parkUnparkService.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException ie)
		{
			System.out.printf("Exception in shutting down thread pools: %s%n", ie.getLocalizedMessage());
			if (!socketProcessorService.isShutdown())
				socketProcessorService.shutdownNow();
			if (!parkUnparkService.isShutdown())
				parkUnparkService.shutdownNow();
		}
		System.out.println("Server shutdown complete");
	}
}