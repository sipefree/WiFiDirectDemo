package wifidirect;

import java.io.Serializable;

public class PlayerInfo implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4115975006279444604L;

	private final String address;

	private final String username;

	private final boolean groupowner;

	public PlayerInfo(String address, String username, boolean groupowner)
	{
		super();
		this.address = address;
		this.username = username;
		this.groupowner = groupowner;
	}

	public boolean isGroupowner()
	{
		return groupowner;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

	/**
	 * @return the address
	 */
	public String getAddress()
	{
		return address;
	}

	/**
	 * @return the username
	 */
	public String getUsername()
	{
		return username;
	}
	
	@Override
	public String toString()
	{
		return address;
	}

}
