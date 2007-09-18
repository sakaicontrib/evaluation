/******************************************************************************
 * ArrayUtils.java - created by aaronz on 12 Mar 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.utils;

/**
 * Utils for working with arrays (these are basically convenience methods)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ArrayUtils {

	/**
	 * Append an item to the end of an array and return the new array
	 * 
	 * @param array an array of items
	 * @param value the item to append to the end of the new array
	 * @return a new array with value in the last spot
	 */
	public static String[] appendArray(String[] array, String value) {
		String[] newArray = new String[array.length + 1];
		System.arraycopy( array, 0, newArray, 0, array.length );
		newArray[newArray.length-1] = value;
		return newArray;
	}

	/**
	 * Append an item to the end of an array and return the new array
	 * 
	 * @param array an array of items
	 * @param value the item to append to the end of the new array
	 * @return a new array with value in the last spot
	 */
	public static Object[] appendArray(Object[] array, Object value) {
		Object[] newArray = new Object[array.length + 1];
		System.arraycopy( array, 0, newArray, 0, array.length );
		newArray[newArray.length-1] = value;
		return newArray;
	}

	/**
	 * Append an item to the end of an array and return the new array
	 * 
	 * @param array an array of items
	 * @param value the item to append to the end of the new array
	 * @return a new array with value in the last spot
	 */
	public static int[] appendArray(int[] array, int value) {
		int[] newArray = new int[array.length + 1];
		System.arraycopy( array, 0, newArray, 0, array.length );
		newArray[newArray.length-1] = value;
		return newArray;
	}

	/**
	 * Prepend an item to the front of an array and return the new array
	 * 
	 * @param array an array of items
	 * @param value the item to prepend to the front of the new array
	 * @return a new array with value in the first spot
	 */
	public static String[] prependArray(String[] array, String value) {
		String[] newArray = new String[array.length + 1];
		System.arraycopy( array, 0, newArray, 1, array.length );
		newArray[0] = value;
		return newArray;
	}

	/**
	 * Prepend an item to the front of an array and return the new array
	 * 
	 * @param array an array of items
	 * @param value the item to prepend to the front of the new array
	 * @return a new array with value in the first spot
	 */
	public static Object[] prependArray(Object[] array, Object value) {
		Object[] newArray = new Object[array.length + 1];
		System.arraycopy( array, 0, newArray, 1, array.length );
		newArray[0] = value;
		return newArray;
	}

	/**
	 * Prepend an item to the front of an array and return the new array
	 * 
	 * @param array an array of items
	 * @param value the item to prepend to the front of the new array
	 * @return a new array with value in the first spot
	 */
	public static int[] prependArray(int[] array, int value) {
		int[] newArray = new int[array.length + 1];
		System.arraycopy( array, 0, newArray, 1, array.length );
		newArray[0] = value;
		return newArray;
	}

	/**
	 * Take an array of anything and turn it into a string
	 * 
	 * @param array any array
	 * @return a string representing that array
	 */
	public static String arrayToString(Object[] array) {
	   StringBuilder result = new StringBuilder();
	   for (int i = 0; i < array.length; i++) {
         if (i > 0) {
            result.append(",");
         }
         result.append(array[i].toString());
      }
	   return result.toString();
	}

}
