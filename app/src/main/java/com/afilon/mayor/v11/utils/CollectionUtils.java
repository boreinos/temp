package com.afilon.mayor.v11.utils;

public class CollectionUtils {

	private CollectionUtils() {
	}

	public static String[] join(String[]... parms) {

		int size = 0;
		for (String[] array : parms) {
			size += array.length;
		}

		String[] result = new String[size];

		int j = 0;
		for (String[] array : parms) {
			for (String s : array) {
				result[j++] = s;
			}
		}
		return result;
	}

}