package com.glucopred.service;

import java.io.Serializable;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public abstract class USBMessage implements Serializable {

	private static final long serialVersionUID = 6137332978312804796L;
	private USBTypes type = null;
	private int datasize = 0;
	final String ALGO = "AES";
	final byte[] iv = new byte[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5'};
    //final byte[] usbKey = new byte[] { 'T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r','e', 't', 'K', 'e', 'y' }; // http://www.random.org/bytes/ 
    final byte[] bluetoothKey = new byte[] { 0x17, 0x61, 0x34, 0x0F, 0x1D, 0x27, 0x38, 0x46, 0x6B, 0x15, 0x5D, 0x6A, 0x63, 0x74, 0x06, 0x7E };
	
	public USBMessage(USBTypes type) {
		this.type = type;
	}
	
	public USBMessage(USBTypes type, int datasize) {
		this.type = type;
		this.datasize = datasize;
	}
	
	public boolean isFinishMessage() {
		return (datasize == 0);
	}
	
	public USBTypes getType() {
		return type;
	}
	
	public void setModel(USBTypes type) {
		this.type = type;
	}
	
	protected byte[] decode(byte[] buffer) throws Exception {
		// Decode incoming packet
	    Key key = new SecretKeySpec(bluetoothKey, ALGO);
	    Cipher cipher;
	    
//	    System.err.print("ENC: ");
//        for(int i=0; i<buffer.length; i++)
//        {
//            int v = buffer[i];
//            if (v<0) v = v+256;
//            String hs = Integer.toHexString(v);
//            if (v<16) 
//                System.err.print("0");
//            System.err.print(hs + " ");
//        }
//        System.err.println();

		cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
	    byte[] plainText = new byte[cipher.getOutputSize(buffer.length)];
	    int ptLength = cipher.update(buffer, 0, buffer.length, plainText, 0);
	    ptLength += cipher.doFinal(plainText, ptLength);
	    
//		System.err.print("DEC: ");
//        for(int i=0; i<plainText.length; i++)
//        {
//            int v = plainText[i];
//            if (v<0) v = v+256;
//            String hs = Integer.toHexString(v);
//            if (v<16) 
//                System.err.print("0");
//            System.err.print(hs + " ");
//        }
//        System.err.println();
	    
	    return plainText;
	}
	


}
