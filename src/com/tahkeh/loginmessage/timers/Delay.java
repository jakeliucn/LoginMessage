package com.tahkeh.loginmessage.timers;

import java.util.List;
import java.util.TimerTask;

import org.bukkit.entity.Player;

import com.tahkeh.loginmessage.Message;
import com.tahkeh.loginmessage.methods.variables.bukkit.BukkitVariables;
import com.tahkeh.loginmessage.timers.Cooldown.CooldownTask;

//This class acts like a TimerTask, but it uses a constructor so I can get vital variables to be used in run()
public class Delay extends TimerTask {
	private final Message msg;
	private final String[] lines;
	private final List<Player> receivers;
	private final CooldownTask task;
	private final BukkitVariables variables;

	public Delay(Message msg, String[] lines, List<Player> receivers, CooldownTask task, BukkitVariables variables) {
		this.msg = msg;
		this.lines = lines;
		this.receivers = receivers;
		this.task = task;
		this.variables = variables;
	}

	public void run() {
		msg.sendMessage(receivers, lines, task, variables);
	}

}
