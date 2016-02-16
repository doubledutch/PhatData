package me.doubledutch.phat.aggregates;

import javax.script.*;
import java.io.*;
import java.security.MessageDigest;

public class JavascriptMapFunction extends JavascriptFunction{
	private String hash;

	public JavascriptMapFunction(String script) throws Exception{
		super();
        jsEngine.eval(script);
        // Bindings bindings = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
    	// bindings.put("stdout", MonolithServer.scriptOut);
        // hash=Checksum.calculateStringMD5(script);
	}

	public String map(String str) throws Exception{
		jsEngine.eval("var obj="+str+";");
		jsEngine.eval("var result=map(obj);");
		jsEngine.eval("if(result!=null)result=JSON.stringify(result);");
		Object obj=jsEngine.eval("result");
		if(obj==null)return null;
		return (String)obj;
	}

	// public String getScriptHash(){
	//	return hash;
	// }
}