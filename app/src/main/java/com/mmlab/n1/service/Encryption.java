package com.mmlab.n1.service;

import android.content.Context;
import android.util.Base64;


import com.mmlab.n1.R;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by waynewei on 2015/11/2.
 */
public class Encryption {


	public static String decode(Context context, String response, String ip){
		byte[] encode = getBase64(response);
		return new String(DecryptAES( context.getString(R.string.decrypt_deh), generateKey( ip, 0), encode));
	}

	public static byte[] getBase64(String response){
		try {
			return Base64.decode(response.getBytes(), Base64.DEFAULT);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] DecryptAES(String ivString, byte[] key, byte[] text) {

		try {

			byte[] iv = ivString.getBytes();

			AlgorithmParameterSpec mAlgorithmParameterSpec = new IvParameterSpec(iv);
			SecretKeySpec mSecretKeySpec = new SecretKeySpec(key, "AES");
			Cipher mCipher = Cipher.getInstance("AES/CBC/ZeroBytePadding");
			mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mAlgorithmParameterSpec);


			return mCipher.doFinal(text);
		} catch (Exception ex) {
			return null;
		}
	}

	public static byte[] generateKey(String IP, int whichCase) {
		String[] ipStringArray = IP.split("\\.");

		int[] ipIntArray = new int[ipStringArray.length];
		ipIntArray[0] = Integer.parseInt(ipStringArray[0]);
		ipIntArray[1] = Integer.parseInt(ipStringArray[1]);
		ipIntArray[2] = Integer.parseInt(ipStringArray[2]);
		ipIntArray[3] = Integer.parseInt(ipStringArray[3]);
		if (whichCase == 2) {
			byte[] a = {(byte) (char) ipIntArray[0], (byte) (char) ipIntArray[1], (byte) (char) ipIntArray[2], (byte) (char) ipIntArray[3],
					(byte) (char) ipIntArray[3], (byte) (char) ipIntArray[2], (byte) (char) ipIntArray[1], (byte) (char) ipIntArray[0],
					(byte) (char) ipIntArray[1], (byte) (char) ipIntArray[3], (byte) (char) ipIntArray[0], (byte) (char) ipIntArray[2],
					(byte) (char) ipIntArray[2], (byte) (char) ipIntArray[1], (byte) (char) ipIntArray[0], (byte) (char) ipIntArray[3]};
			return a;
		} else {
			byte[] a = {(byte) (char) ipIntArray[0], (byte) (char) ipIntArray[1], (byte) (char) ipIntArray[2], (byte) (char) ipIntArray[3],
					(byte) (char) ipIntArray[3], (byte) (char) ipIntArray[2], (byte) (char) ipIntArray[1], (byte) (char) ipIntArray[0],
					(byte) (char) ipIntArray[1], (byte) (char) ipIntArray[3], (byte) (char) ipIntArray[0], (byte) (char) ipIntArray[2],
					(byte) (char) ipIntArray[2], (byte) (char) ipIntArray[1], (byte) (char) ipIntArray[0], (byte) (char) ipIntArray[3]};
			return a;
		}

	}
}
