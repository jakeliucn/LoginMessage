package com.tahkeh.loginmessage.methods;


import org.bukkit.entity.Player;

public interface Method {

	/**
	 * Executes the method and returns null if the parameters are invalid.
	 * @param parameters the parameters.
	 * @return the parsed string or null if invalid.
	 */
	String call(Player player, String event, String... parameters);

	/**
	 * Defines if this method has recursive calls.
	 * @return if this method has recursive calls.
	 */
	boolean recursive();
}