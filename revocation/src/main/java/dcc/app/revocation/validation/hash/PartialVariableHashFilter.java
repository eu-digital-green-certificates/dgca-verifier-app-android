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
 *  Created by mykhailo.nester on 07/03/2022, 17:15
 */

package dcc.app.revocation.validation.hash;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class PartialVariableHashFilter {

    private List<BigInteger> arrayList;
    private byte size;
    private float probRate;
    private int definedElementAmount;
    private int currentElementAmount;

    private static final short version = 1;

    /**
     * Partial variable hash list filter initialization
     *
     * @param data bytearray of partial variable hashes
     */
    public PartialVariableHashFilter(byte[] data) {
        readFrom(data);
    }

    /**
     * Partial variable hash list filter initialization
     *
     * @param minSize          minimum size of the filter
     * @param partitionOffset  coordinate = 16, vector = 8, point = 0
     * @param numberOfElements elements in the filter
     * @param propRate         probability rate
     * @see PartitionOffset
     */
    public PartialVariableHashFilter(byte minSize, @NotNull PartitionOffset partitionOffset, int numberOfElements, float propRate) {
        byte actualSize = calc(partitionOffset.value, numberOfElements, propRate);

        this.definedElementAmount = numberOfElements;
        this.currentElementAmount = 0;
        this.arrayList = new ArrayList<>();
        this.probRate = propRate;

        if (actualSize < minSize) {
            size = minSize;
        } else {
            size = actualSize;
        }
    }

    private byte calc(byte partitionOffset, int numberOfElements, float propRate) {
        double num = Math.ceil(Math.log10(numberOfElements) / Math.log10(2));
        double rounded = num / 8 + num % 8;
        return (byte) Math.ceil(rounded - partitionOffset + propRate);
    }

    private void readFrom(byte @NotNull [] data) {
        arrayList = new ArrayList<>();

        if (data.length == 0) {
            return;
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        try {
            int version = dataInputStream.readShort(); // for later compatibility
            probRate = dataInputStream.readFloat();
            definedElementAmount = dataInputStream.readInt();
            size = dataInputStream.readByte();
            currentElementAmount = 0;

            byte[] pvh = new byte[size];

            int offset = 0;
            int numberOfBytesRead;

            while (true) {
                numberOfBytesRead = dataInputStream.read(pvh, offset, size - offset);

                if (numberOfBytesRead < 0) {
                    //end of data reached
                    break;
                } else {
                    offset += numberOfBytesRead;
                    if (offset == size) {
                        arrayList.add(new BigInteger(pvh));
                        offset = 0;
                        currentElementAmount++;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(arrayList);
    }

    public byte[] writeTo() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        // write header bytes
        dataOutputStream.writeShort(version);
        dataOutputStream.writeFloat(this.probRate);
        dataOutputStream.writeInt(this.definedElementAmount);
        dataOutputStream.writeByte(this.size);

        // write data
        for (BigInteger bigInteger : arrayList) {
            if (bigInteger != null) {
                byte[] bytes = bigInteger.toByteArray();
                int length = bytes.length;

                while (length < size) {
                    outputStream.write(0);
                    length++;
                }

                dataOutputStream.write(bytes);
            }
        }

        return outputStream.toByteArray();
    }

    /**
     * Add hash data into searchable array of BigIntegers.
     *
     * @param data binary hash data
     * @throws IllegalArgumentException when data is less than partial hash size
     */
    public void add(byte @NotNull [] data) throws IllegalArgumentException {

        if (data.length < size) {
            throw new IllegalArgumentException("Data length cannot be less than partial hash size");
        }

        if (currentElementAmount >= definedElementAmount) {
            Logger.getGlobal().warning("Filter has more elements than expected. " +
                "It may result in a higher False Positive Rate than defined!");
        }

        arrayList.add(new BigInteger(Arrays.copyOf(data, size)));
        currentElementAmount++;
        Collections.sort(arrayList);
    }

    /**
     * Check whether filter contains dcc hash bytes. It will check bytes depending on the filter size value.
     *
     * @param dccHashBytes byte array of dcc hash.
     * @return true is contains otherwise false
     */
    public boolean mightContain(@NonNull byte[] dccHashBytes) {
        if (dccHashBytes.length < size) {
            return false;
        }

        return new BinarySearch().binarySearch(
            arrayList.toArray(arrayList.toArray(new BigInteger[0])),
            0,
            arrayList.size(),
            new BigInteger(Arrays.copyOf(dccHashBytes, size)));
    }

    public byte getSize() {
        return size;
    }

    public BigInteger[] getArray() {
        return arrayList.toArray(new BigInteger[0]);
    }

    public int getElementsCount() {
        return currentElementAmount;
    }
}