/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utils for working with collections and arrays (these are basically convenience methods)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ArrayUtils {

    /**
     * Remove all duplicate objects from a list
     * 
     * @param list
     * @return the original list with the duplicate objects removed
     */
    public static <T> List<T> removeDuplicates(List<T> list) {
        Set<T> s = new HashSet<>();
        for (Iterator<T> iter = list.iterator(); iter.hasNext();) {
            T element = (T) iter.next();
            if (! s.add(element)) {
                iter.remove();
            }
        }
        return list;
    }

    /**
     * Remove all duplicate objects from an array and ensure everything
     * in the array is unique, order is maintained
     * 
     * @param array any array
     * @return a new array with the duplicate objects removed OR the original array if no duplicates exist
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] unique(T[] array) {
        if (array == null || array.length == 0) {
            return array;
        }
        LinkedHashSet<T> s = new LinkedHashSet<>(array.length);
        Class<?> type = array.getClass().getComponentType();
        s.addAll( Arrays.asList( array ) );
        T[] newArray;
        if (s.size() == array.length) {
            newArray = array;
        } else {
            newArray = (T[]) Array.newInstance(type, array.length);
            int pos = 0;
            for (T t : s) {
                newArray[pos] = t;
            }
        }
        return newArray;
    }

    /**
     * Make a copy of an array
     * 
     * @param <T>
     * @param array an array of objects
     * @return a copy of the array
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] copy(T[] array) {
        Class<?> type = array.getClass().getComponentType();
        T[] newArray = (T[]) Array.newInstance(type, array.length);
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }

    /**
     * Checks to see if an array contains a value,
     * will return false if a null value is supplied
     * 
     * @param <T>
     * @param array any array of objects
     * @param value the value to check for
     * @return true if the value is found, false otherwise
     */
    public static <T> boolean contains(T[] array, T value) {
        boolean foundValue = false;
        if (value != null) {
            for( T array1 : array )
            {
                if( value.equals( array1 ) )
                {
                    foundValue = true;
                    break;
                }
            }
        }
        return foundValue;
    }

    /**
     * Checks to see if an array contains a value
     * 
     * @param array
     * @param value
     * @return true if the value is found, false otherwise
     */
    public static boolean contains(int[] array, int value) {
        boolean foundValue = false;
        for (int i = 0; i < array.length; i++) {
            if (value == array[i]) {
                foundValue = true;
                break;
            }
        }
        return foundValue;
    }

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
    @SuppressWarnings("unchecked")
    public static <T> T[] appendArray(T[] array, T value) {
        Class<?> type = array.getClass().getComponentType();
        T[] newArray = (T[]) Array.newInstance(type, array.length + 1);
        System.arraycopy( array, 0, newArray, 0, array.length );
        newArray[newArray.length-1] = value;
        return newArray;
    }

    /**
     * Append an array to another array
     * 
     * @param array1 an array of items
     * @param array2 an array of items
     * @return a new array with array1 first and array2 appended on the end
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] appendArrays(T[] array1, T[] array2) {
        Class<?> type = array1.getClass().getComponentType();
        T[] newArray = (T[]) Array.newInstance(type, array1.length + array2.length);
        System.arraycopy( array1, 0, newArray, 0, array1.length );
        System.arraycopy( array2, 0, newArray, array1.length, array2.length );
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
    @SuppressWarnings("unchecked")
    public static <T> T[] prependArray(T[] array, T value) {
        Class<?> type = array.getClass().getComponentType();
        T[] newArray = (T[]) Array.newInstance(type, array.length + 1);
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
        if (array != null && array.length > 0) {
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    result.append(",");
                }
                if (array[i] != null) {
                    result.append(array[i].toString());
                }
            }
        }
        return result.toString();
    }

    /**
     * Take a list of number objects and return an int[] array
     * @param list any list of {@link Number}
     * @return an array of int
     */
    public static int[] listToIntArray(List<Number> list) {
        int[] newArray = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            newArray[i] = list.get(i).intValue();
        }
        return newArray;
    }

}
