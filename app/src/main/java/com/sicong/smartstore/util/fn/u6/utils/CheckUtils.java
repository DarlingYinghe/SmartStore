package com.sicong.smartstore.util.fn.u6.utils;

public class CheckUtils {
		public static boolean checkBin(String binstr, int len){
			
			if(binstr == null || binstr.equals(""))
			{
				return false;
			}
			
			if(binstr.length() != len)
			{
				return false;
			}
			
			String pattern = "^[01]{"+len+"}$";
			return binstr.matches(pattern);
		}
}
