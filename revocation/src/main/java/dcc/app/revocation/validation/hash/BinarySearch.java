/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-partial-hash-filter
 *  ---
 *  Copyright (C) 2022 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by mykhailo.nester on 04/03/2022, 16:39
 */

package dcc.app.revocation.validation.hash;

import java.math.BigInteger;

public class BinarySearch {

    /**
     * Searches the array or the range of the array for the provided [element] using the binary search algorithm.
     * The array is expected to be sorted, otherwise the result is undefined.
     * <p>
     * If the array contains multiple elements equal to the specified [element], there is no guarantee which one will be found.
     *
     * @param element   the to search for.
     * @param fromIndex the start of the range (inclusive) to search in, 0 by default.
     * @param toIndex   the end of the range (exclusive) to search in, size of this array by default.
     * @return the index of the element, if it is contained in the array within the specified range;
     */
    public boolean binarySearch(BigInteger[] array, int fromIndex, int toIndex, BigInteger element) {
        if (toIndex >= fromIndex) {
            int middle = fromIndex + (toIndex - fromIndex) / 2;

            if (middle < 0 || middle >= array.length) {
                return false;
            }

            if (array[middle].compareTo(element) == 0) {
                return true;
            }

            if (array[middle].compareTo(element) > 0) {
                return binarySearch(array, fromIndex, middle - 1, element);
            }

            return binarySearch(array, middle + 1, toIndex, element);
        }

        return false;
    }
}