package com.tahkeh.loginmessage.methods.impl.bukkit;

import com.tahkeh.loginmessage.Message;
import com.tahkeh.loginmessage.methods.DefaultMethod;
import com.tahkeh.loginmessage.methods.DefaultNamedMethod;
import com.tahkeh.loginmessage.methods.parameter.Parameter;
import com.tahkeh.loginmessage.methods.variables.Variables;

public class ColorMethod extends DefaultNamedMethod<Variables> {

	public ColorMethod() {
		super(true, "color", 1);
	}

	@Override
	public String call(Parameter[] parameters, Variables globalParameters) {
		if (parameters.length == 1) {
			Integer i = DefaultMethod.parseAsInteger(parameters[0].parse());
			if (i != null) {
				return Message.SECTION_SIGN + Integer.toHexString(i);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

}