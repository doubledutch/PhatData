package me.doubledutch.phatdata;

import java.io.*;
import java.net.*;
import org.json.*;

public interface StreamConnection{
	public String get(long index,long endIndex) throws IOException;
	public String get(long index) throws IOException;
	public String getLast() throws IOException;
	public long append(JSONObject data) throws IOException,JSONException;
	public long append(String data) throws IOException,JSONException;
	public void truncate(long index) throws IOException;
}