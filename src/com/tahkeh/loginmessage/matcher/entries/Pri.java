package com.tahkeh.loginmessage.matcher.entries;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Pri implements Entry {

	private final boolean positive;
	private final Player trigger;

	public Pri(boolean positive, Player trigger) {
		this.positive = positive;
		this.trigger = trigger;
	}

	@Override
	public boolean isPositive() {
		return this.positive;
	}

	@Override
	public boolean match(OfflinePlayer player) {
		return this.trigger.equals(player);
	}

}
