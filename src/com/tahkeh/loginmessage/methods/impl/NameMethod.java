package com.tahkeh.loginmessage.methods.impl;

import org.bukkit.OfflinePlayer;

import com.tahkeh.loginmessage.methods.OriginalMethod;
import com.tahkeh.loginmessage.methods.variables.Variables;

public class NameMethod extends OriginalMethod {

	public NameMethod() {
		super("nm");
	}

	@Override
	protected String call(OfflinePlayer trigger, Variables globalParameters) {
		return trigger.getName();
	}

}