package me.doubledutch.phat.aggregates;

import org.json.*;

public class MapResult{
	private String in,out,error;
	private long time;

	public MapResult(String in,String out,String error,long time){
		this.in=in;
		this.out=out;
		this.error=error;
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject obj=new JSONObject();
		obj.put("in",in);
		obj.put("out",out);
		obj.put("error",error);
		obj.put("time",time);
		return obj;
	}
}