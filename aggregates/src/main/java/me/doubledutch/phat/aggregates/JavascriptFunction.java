package me.doubledutch.phat.aggregates;

import java.io.*;
import javax.script.*;

public class JavascriptFunction{
	protected ScriptEngine jsEngine;
	protected Invocable jsInvocable;

	public JavascriptFunction(){
		ScriptEngineManager mgr = new ScriptEngineManager();
        jsEngine = mgr.getEngineByName("JavaScript");
        // jsEngine = mgr.getEngineByName("nashorn");
        jsInvocable = (Invocable) jsEngine;
	}
}