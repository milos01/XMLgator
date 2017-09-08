package com.example.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;

public class Util {

	private static SecureRandom random = new SecureRandom();

	public static String nextID() {
	    return new BigInteger(130, random).toString(32);
	}
	
	public static String readFileString(String fileName){
		try {
			InputStream is = new FileInputStream(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			String line = reader.readLine();
			StringBuilder sb = new StringBuilder();
			
			while (line != null){
				sb.append(line + '\n');
				line = reader.readLine();
			}
			reader.close();
			
			return sb.toString();
		} catch (IOException e){
			return null;
		}
	}
}
