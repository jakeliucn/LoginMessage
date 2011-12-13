package com.tahkeh.loginmessage.methods.impl.bukkit;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.tahkeh.loginmessage.methods.DefaultMethod;
import com.tahkeh.loginmessage.methods.DefaultNamedMethod;
import com.tahkeh.loginmessage.methods.parameter.Parameter;
import com.tahkeh.loginmessage.methods.variables.bukkit.BukkitVariables;

public class LocationMethod extends DefaultNamedMethod<BukkitVariables> {

	public LocationMethod() {
		super(true, "location", 0, 1, 2, 3);
	}

	@Override
	public String call(Parameter[] parameters, BukkitVariables globalParameters) {
		if (globalParameters.offlinePlayer instanceof Player) {
			Location location = ((Player) globalParameters.offlinePlayer).getLocation();
			String format = null;
			boolean[] set = new boolean[3];
			switch (parameters.length) {
			case 0 :
				Arrays.fill(set, true);
				break;
			case 3 :
				Boolean boolZ = DefaultMethod.parseAsBoolean(parameters[2].parse());
				if (boolZ == null) {
					set = null;
					break;
				} else {
					set[2] = boolZ;
				}
			case 2 :
				Boolean boolY = DefaultMethod.parseAsBoolean(parameters[1].parse());
				if (boolY == null) {
					set = null;
					break;
				} else if (set != null) {
					set[2] = boolY;
				}
			case 1 :
				Boolean boolX = DefaultMethod.parseAsBoolean(parameters[0].parse());
				if (boolX == null && parameters.length == 1) {
					format = parameters[0].parse();
					set = null;
					break;
				} else if (set != null) {
					set[0] = boolX;
				}
			}
			if (format == null) {
				if (set == null) {
					// :( Invalid format
					return null;
				} else {
					for (int i = 0; i < set.length; i++) {
						if (set[i]) {
							if (format == null) {
								format = "";
							} else {
								format += ", ";
							}
							format += "%" + (i + 1) + "$";
						}
					}
				}
			}
			
			if (format != null) {
				return String.format(format, location.getBlockX(), location.getBlockY(), location.getBlockZ());
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

}