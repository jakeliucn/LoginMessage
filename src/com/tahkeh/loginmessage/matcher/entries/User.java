package com.tahkeh.loginmessage.matcher.entries;

import org.bukkit.OfflinePlayer;

public class User extends DefaultEntry {

	public User(String user) {
		super(user);
	}

	public boolean match(OfflinePlayer player) {
		return player.getName().equals(this.signedTextData.unsignedText);
	}
}