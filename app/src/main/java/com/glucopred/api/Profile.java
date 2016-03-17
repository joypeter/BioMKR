package com.glucopred.api;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.types.BasicBSONList;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // http://stackoverflow.com/questions/5455014/ignoring-new-fields-on-json-objects-using-jackson
public class Profile {
		//public ObjectId id;
//		public Integer _version;
		public String patientid; // ObjectId
		public String experimentid; // ObjectId
		public String name;
		public Date date;   
		public String method;
		public String method_reference;
		public String point;
		public String type;
		public List<Object> x;
		public List<Float> y;
		public String ylabel;
		public String xlabel;
		public String yunit;
		public String xunit;
		public String subtype;
		
		final String ALGO = "AES";
		final byte[] iv = new byte[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5'};
	    //final byte[] usbKey = new byte[] { 'T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r','e', 't', 'K', 'e', 'y' }; // http://www.random.org/bytes/ 
	    final byte[] bluetoothKey = new byte[] { 0x17, 0x61, 0x34, 0x0F, 0x1D, 0x27, 0x38, 0x46, 0x6B, 0x15, 0x5D, 0x6A, 0x63, 0x74, 0x06, 0x7E };
		
		public Profile(String patientid, String experimentid, String name, Date date, String method, String method_reference, String point, String type, String subtype, String ylabel, String xlabel, String yunit, String xunit) {
			this.patientid = patientid;
			this.experimentid =  experimentid;
			this.name = name;
			this.date = date;
			this.method = method;
			this.method_reference = method_reference;
			this.point = point;
			this.type = type;
			this.subtype = subtype;
			this.ylabel = ylabel;
			this.xlabel = xlabel;
			this.yunit = yunit;
			this.xunit = xunit;
		}
		
		protected byte[] decode(byte[] buffer) throws Exception {
			// Decode incoming packet
		    Key key = new SecretKeySpec(bluetoothKey, ALGO);
		    Cipher cipher;
		    
//		    System.err.print("ENC: ");
//	        for(int i=0; i<buffer.length; i++)
//	        {
//	            int v = buffer[i];
//	            if (v<0) v = v+256;
//	            String hs = Integer.toHexString(v);
//	            if (v<16) 
//	                System.err.print("0");
//	            System.err.print(hs + " ");
//	        }
//	        System.err.println();

			cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		    byte[] plainText = new byte[cipher.getOutputSize(buffer.length)];
		    int ptLength = cipher.update(buffer, 0, buffer.length, plainText, 0);
		    ptLength += cipher.doFinal(plainText, ptLength);
		    
//			System.err.print("DEC: ");
//	        for(int i=0; i<plainText.length; i++)
//	        {
//	            int v = plainText[i];
//	            if (v<0) v = v+256;
//	            String hs = Integer.toHexString(v);
//	            if (v<16) 
//	                System.err.print("0");
//	            System.err.print(hs + " ");
//	        }
//	        System.err.println();
		    
		    return plainText;
		}
		
		public Profile(String patientid, String experimentid, byte[] bson_bin) throws Exception {
			this.patientid = patientid;
			this.experimentid = experimentid;
			
			// Decode BSON string
			BasicBSONDecoder decoder = new BasicBSONDecoder();
			BSONObject bsonObject = null;
			try {
				bsonObject = decoder.readObject(bson_bin);
			} catch (Exception E) {
				byte[] buffer = decode(bson_bin);
				bsonObject = decoder.readObject(buffer);
			}
//			String json_string = bsonObject.toString();
			//System.out.println(json_string);
			
			// Assign single
			this.name = bsonObject.get("N").toString();
			this.date = new Date(Long.parseLong(bsonObject.get("T").toString()) * 1000);
			
			// Assign arrays
			BasicBSONList Xs = (BasicBSONList) bsonObject.get("D");
			BasicBSONList YsM = (BasicBSONList) bsonObject.get("V");
			BasicBSONList Ys = (BasicBSONList) YsM.get(0); // Interested in first row only for now

			this.x = new ArrayList<Object>();
			this.y = new ArrayList<Float>();
		
			for (int i = 0; i < Xs.size(); i++) {
				this.x.add(Xs.get(i).toString());
				this.y.add(Float.parseFloat(Ys.get(i).toString()));
			 }
			
			float rrsd = Float.parseFloat(bsonObject.get("R").toString());
			float rhotel = Float.parseFloat(bsonObject.get("H").toString()); 
			
			if (rrsd != 0 && rhotel != 0) {
				this.x.add("rrsd");
				this.y.add(rrsd);
				this.x.add("rhotelling");
				this.y.add(rhotel);
			}
		}

//		public void setData(String subtype, double[] y) {
//			this.x = new ArrayList<Object>();
//			this.y = new ArrayList<Float>();
//			for (int i = 0; i < y.length; i++) {
//				this.x.add(subtype + "-" + Double.valueOf(i).toString());
//				this.y.add((float)y[i]);
//			 }
//		}
		
		public void setData(String subtype, float[] y) {
			this.x = new ArrayList<Object>();
			this.y = new ArrayList<Float>();
			for (int i = 0; i < y.length; i++) {
				this.x.add(subtype + "-" + Double.valueOf(i).toString());
				this.y.add(y[i]);
//				this.y.add(Utils.floatToDouble(y[i]));
			 }
		}
		
		public void setData(String subtype, float[] x, float[] y) {
			//TODO  NB! x.length must be == y.length 
			this.x = new ArrayList<Object>();
			this.y = new ArrayList<Float>();
			for (int i = 0; i < y.length; i++) {
				this.x.add(subtype + "-" + x[i] + "-" + Double.valueOf(i).toString());
				this.y.add(y[i]);
//				this.y.add(Utils.floatToDouble(y[i]));
			 }
		}
		
//		public void setData(String[] x, double[] y) {
//			this.x = new ArrayList<Object>();
//			this.y = new ArrayList<Double>();
//			for (int i = 0; i < y.length; i++) {
//				this.x.add(x[i]);
//				this.y.add(y[i]);
//			 }
//		}
		
		public void setData(String[] x, float[] y) {
			this.x = new ArrayList<Object>();
			this.y = new ArrayList<Float>();
			for (int i = 0; i < y.length; i++) {
				this.x.add(x[i]);
				this.y.add(y[i]);
			 }
		}
		
//
//		public void setData(Object x, double y) {
//			this.x = new ArrayList<Object>();
//			this.y = new ArrayList<Double>();
//			this.x.add(x);
//			this.y.add(y);			
//		}
		
		public void setData(Object x, float y) {
			this.x = new ArrayList<Object>();
			this.y = new ArrayList<Float>();
			this.x.add(x);
			this.y.add(y);			
		}
		
}
