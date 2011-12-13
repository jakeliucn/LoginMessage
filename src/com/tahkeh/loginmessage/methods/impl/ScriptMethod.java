package com.tahkeh.loginmessage.methods.impl;

import java.io.Reader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


import com.tahkeh.loginmessage.methods.Method;
import com.tahkeh.loginmessage.methods.parameter.Parameter;
import com.tahkeh.loginmessage.methods.variables.Variables;

import de.xzise.XLogger;

public class ScriptMethod implements Method<Variables> {

	private final Invocable invocable;
	private final String methodName;
	private final boolean recursive;
	private final XLogger logger;

	public ScriptMethod(final String methodName, final Invocable invocable, final boolean recursive, final XLogger logger) {
		this.invocable = invocable;
		this.methodName = methodName;
		this.recursive = recursive;
		this.logger = logger;
	}

	public static ScriptMethod create(final String engineName, final String methodName, final Reader reader, final ScriptEngineManager engineManager, final boolean recursive, final XLogger logger) {
		ScriptEngine engine = getEngine(engineName, reader, engineManager, logger);
		if (engine instanceof Invocable) {
			return new ScriptMethod(methodName, (Invocable) engine, recursive, logger);
		} else {
			return null;
		}
	}

	public static ScriptEngine getEngine(final String name, final Reader reader, final ScriptEngineManager engineManager, final XLogger logger) {
		ScriptEngine engine = engineManager.getEngineByName(name);
		try {
			engine.eval(reader);
		} catch (ScriptException e) {
			engine = null;
			logger.warning("Unable to evaluate script.", e);
		}
		return engine;
	}

	@Override
	public String call(Parameter[] parameters, Variables globalParameters) {
		Object result = null;
		try {
			result = this.invocable.invokeFunction(this.methodName, parameters, globalParameters);
		} catch (ScriptException e) {
			this.logger.warning("Unable to call '" + this.methodName + "(Parameter[], Variables)'!", e);
		} catch (NoSuchMethodException e) {
			this.logger.warning("No such method named '" + this.methodName + "(Parameter[], Variables)'!", e);
		}
		return result == null ? null : result.toString();
	}

	@Override
	public boolean isRecursive() {
		return this.recursive;
	}

}