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
 *  Created by mykhailo.nester on 07/03/2022, 18:43
 */

package dcc.app.revocation.hashfilter;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import dcc.app.revocation.validation.hash.PartialVariableHashFilter;
import dcc.app.revocation.validation.hash.PartitionOffset;

public class PartialVariableHashFilterTest {

    @Test
    public void mightContainTest() {
        byte[] byteArray = new byte[]{0, 1, 45, 47, -21, -1, 0, 0, 0, 7, 2, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14}; // length = 2
        byte[] searchedArray = new byte[]{3, 4};

        PartialVariableHashFilter filter = new PartialVariableHashFilter(byteArray);
        boolean result = filter.mightContain(searchedArray);
        assert result;
    }

    @Test
    public void filterToBinaryTest() throws IOException {
        byte[] byteArray = new byte[]{0, 1, 45, 47, -21, -1, 0, 0, 0, 7, 2, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14}; // length = 2

        PartialVariableHashFilter filter = new PartialVariableHashFilter(byteArray);
        byte[] result = filter.writeTo();
        assert Arrays.equals(result, byteArray);
    }

    @Test
    public void initPointPartitionHashFilterTest() {
        int numberOfElements = 1000;
        byte minSize = 1;
        PartitionOffset partitionOffset = PartitionOffset.POINT;
        float probRate = 0.000001F;

        PartialVariableHashFilter filter = new PartialVariableHashFilter(minSize, partitionOffset, numberOfElements, probRate);
        byte result = filter.getSize();
        assert result == 4;
    }

    @Test
    public void initVectorPartitionHashFilterTest() {
        int numberOfElements = 1000;
        byte minSize = 1;
        PartitionOffset partitionOffset = PartitionOffset.VECTOR;
        float probRate = 0.000001F;

        PartialVariableHashFilter filter = new PartialVariableHashFilter(minSize, partitionOffset, numberOfElements, probRate);
        byte result = filter.getSize();
        assert result == 3;
    }

    @Test
    public void initCoordinatePartitionHashFilterTest() {
        int numberOfElements = 625000;
        byte minSize = 4;
        PartitionOffset partitionOffset = PartitionOffset.COORDINATE;
        float probRate = 0.000001F;

        PartialVariableHashFilter filter = new PartialVariableHashFilter(minSize, partitionOffset, numberOfElements, probRate);
        byte result = filter.getSize();
        assert result == 5;
    }

    @Test
    public void addHashesToFilterIncorrectBinaryTest() {
        int numberOfElements = 10;
        byte minSize = 1;
        PartitionOffset partitionOffset = PartitionOffset.POINT;
        float probRate = 0.000001F;
        byte[] array = new byte[]{1, 2, 3, 4}; // length = 5

        PartialVariableHashFilter filter = new PartialVariableHashFilter(minSize, partitionOffset, numberOfElements, probRate);
        try {
            filter.add(array);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            System.out.println("Test success");
        }
    }

    @Test
    public void addHashesToFilterTest() {
        int numberOfElements = 10;
        byte minSize = 1;
        PartitionOffset partitionOffset = PartitionOffset.POINT;
        float probRate = 0.000001F;
        byte[] array0 = new byte[]{1, 2, 3, 4, 5};
        byte[] array1 = new byte[]{6, 7, 8, 9, 10};
        BigInteger[] expectedArray = new BigInteger[]{
            new BigInteger("4328719365"),
            new BigInteger("25887770890")
        };

        PartialVariableHashFilter filter = new PartialVariableHashFilter(minSize, partitionOffset, numberOfElements, probRate);
        filter.add(array0);
        filter.add(array1);
        BigInteger[] arrayResult = filter.getArray();

        assert filter.getSize() == 5;
        assert Arrays.equals(arrayResult, expectedArray);
    }

    @Test
    public void addHashesToFilterSortTest() {
        int numberOfElements = 10;
        byte minSize = 4;
        PartitionOffset partitionOffset = PartitionOffset.POINT;
        float probRate = 0.000001F;
        byte[] array0 = new byte[]{6, 7, 8, 9, 10};
        byte[] array1 = new byte[]{1, 2, 3, 4, 5};
        BigInteger[] expectedArray = new BigInteger[]{
            new BigInteger("4328719365"),
            new BigInteger("25887770890")
        };

        PartialVariableHashFilter filter = new PartialVariableHashFilter(minSize, partitionOffset, numberOfElements, probRate);
        filter.add(array0);
        filter.add(array1);

        BigInteger[] arrayResult = filter.getArray();

        assert filter.getSize() == 5;
        assert Arrays.equals(arrayResult, expectedArray);
    }
}
