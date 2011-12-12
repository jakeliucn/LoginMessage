package com.tahkeh.loginmessage.methods.impl;

public class MaximumMethod extends MinMaxMethod {

	public MaximumMethod(final boolean first) {
		super(first, "max");
	}

	@Override
	protected boolean compare(int nHighest, int tested) {
		return nHighest > tested;
	}
}