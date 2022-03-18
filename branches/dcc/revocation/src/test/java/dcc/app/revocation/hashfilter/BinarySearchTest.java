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

package dcc.app.revocation.hashfilter;

import org.junit.Test;

import java.math.BigInteger;

import dcc.app.revocation.validation.hash.BinarySearch;

public class BinarySearchTest {

    @Test
    public void searchArrayInByteArrayTest() {
        BigInteger[] intArray = new BigInteger[]{
            new BigInteger(new byte[]{1, 2}),
            new BigInteger(new byte[]{2, 4}),
            new BigInteger(new byte[]{3, 6}),
            new BigInteger(new byte[]{4, 0}),
            new BigInteger(new byte[]{8, 2})
        };
        int toIndex = intArray.length - 1;
        boolean result = new BinarySearch().binarySearch(intArray, 0, toIndex, new BigInteger(new byte[]{4, 0}));
        assert result;
    }

    @Test
    public void searchArrayNotInByteArrayTest() {
        BigInteger[] intArray = new BigInteger[]{
            new BigInteger(new byte[]{1, 2}),
            new BigInteger(new byte[]{2, 4}),
            new BigInteger(new byte[]{3, 6}),
            new BigInteger(new byte[]{8, 2})
        };
        int toIndex = intArray.length - 1;
        boolean result = new BinarySearch().binarySearch(intArray, 0, toIndex, new BigInteger(new byte[]{0, 6}));
        assert !result;
    }
}