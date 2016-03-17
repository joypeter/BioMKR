package com.glucopred.service;

import java.nio.ByteBuffer;

import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;

public class BSONData extends USBMessage{

	public BSONObject bsonObject = null;
	
	public BSONData(ByteBuffer mp, USBTypes type) throws Exception {
		super(type);

		// Decode BSON
		BasicBSONDecoder decoder = new BasicBSONDecoder();
		
		try {
			bsonObject = decoder.readObject(mp.array());
		} catch (Exception E) {
			byte[] buffer = decode(mp.array());
			bsonObject = decoder.readObject(buffer);
		}
		
		//System.out.println(bsonObject.toString());
		
	}
	
	public Object get(String name) {
		return bsonObject.get(name);
	}
}
