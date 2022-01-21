/*
 * Copyright (c) 2022 T-Systems International GmbH and all other contributors
 * Author: Paul Ballmann
 */

package dcc.app.revocation.validation;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongArray;

public class BloomFilterImpl implements BloomFilter, Serializable {
    private int numBits;
    private int numberOfHashes;
    private double probRate;
    private AtomicLongArray data;
    private final static int NUM_BITS = 8;
    private final static byte NUM_BYTES = Long.BYTES;
    //    @Serial
    private static final long serialVersionUID = 7526472295622776147L;

    public BloomFilterImpl(InputStream inputStream) {
        DataInputStream dis = new DataInputStream(inputStream);
        this.readFromStream(dis);
    }

    public BloomFilterImpl(int size, int numberOfHashes) {
        super();
        if (numberOfHashes == 0) {
            throw new IllegalArgumentException("numberOfHashes cannot be 0");
        }

        size = (size / NUM_BYTES) + (size % NUM_BYTES);
        this.numBits = ((size / NUM_BYTES) + (size % NUM_BYTES)) * (NUM_BITS * NUM_BYTES);
        this.numberOfHashes = numberOfHashes;
        this.probRate = (float) Math.pow(1 - Math.exp(-numberOfHashes / (float) ((float) (this.numBits / NUM_BITS) / this.numBits)), numberOfHashes);
        this.data = new AtomicLongArray(size);
    }

    public BloomFilterImpl(int numberOfElements, int numberOfHashes, double probRate) {
        super();
        if (numberOfElements == 0 || numberOfHashes == 0 || probRate > 1 || probRate == 0) {
            throw new IllegalArgumentException("numberOfElements != 0, numberOfHashes != 0, probRate <= 1");
        }
        // n: numberOfElements
        // m: numberOfBits -> ceil((n * log(p)) / log(1 / pow(2, log(2))));
        this.numBits = (int) (Math.ceil((numberOfElements * Math.log(probRate)) / Math.log(1 / Math.pow(2, Math.log(2)))));

        int bytes = (this.numBits / NUM_BITS) + 1;
        int size = (bytes / NUM_BYTES) + (bytes % NUM_BYTES);

        this.numberOfHashes = numberOfHashes;
        this.probRate = probRate;
        this.data = new AtomicLongArray(size);
    }

    public AtomicLongArray getData() {
        return data;
    }

    @Override
    public void add(byte[] element) throws NoSuchAlgorithmException, IOException {
        for (int i = 0; i < this.numberOfHashes; i++) {
            int index = this.calcIndex(element, i, this.numBits).intValue();
            System.out.println("INDEX: " + index);
            int bytepos = index / (NUM_BYTES * NUM_BITS);
            long pattern = Long.MIN_VALUE >>> index - 1;
            this.data.set(bytepos, this.data.get(bytepos) | pattern);
        }
    }

    @Override
    public boolean contains(byte[] element) throws NoSuchAlgorithmException, IOException {
        for (int i = 0; i < this.numberOfHashes; i++) {
            int index = this.calcIndex(element, i, this.numBits).intValue();
            int bytepos = index / (NUM_BYTES * NUM_BITS);
            long pattern = Long.MIN_VALUE >>> index - 1;
            if ((this.data.get(bytepos) & pattern) == pattern) {
                return true;
            }
        }
        return false;
    }

    public BigInteger calcIndex(byte[] element, int i, long bits) throws NoSuchAlgorithmException, IOException {
        BigInteger bi = new BigInteger(this.hash(element, (char) i));
        return bi.mod(BigInteger.valueOf(bits));
    }

    private byte[] hash(byte[] toHash, char seed) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // concat byte[] and seed
        byte charAsByte = (byte) seed;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(toHash);
        outputStream.write(charAsByte);
        return md.digest(outputStream.toByteArray());
    }


    //region Streams

    /**
     * Writes the filter to an output stream in a structured manner
     * 0 byte -> k (numberOfHashes)
     * 1 - 4 byte -> p (probRate)
     * 5 byte -> unsigned length of the data
     * 6 - x byte -> data as utf8
     *
     * @param outputStream
     * @throws IOException
     */
    public void writeTo(OutputStream outputStream) throws IOException {
        // k = 1 (numberOfHashes), p = 4 (probRate),
        // 0 = k, 1 = p, 5 = filter
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(this.numberOfHashes);
        dataOutputStream.writeDouble(this.probRate);
        dataOutputStream.writeInt(this.getData().length());
        for (int i = 0; i < this.getData().length(); i++) {
            dataOutputStream.writeLong(this.getData().get(i));
        }

    }

    private void readFromStream(DataInputStream dis) {
        try {
            this.numberOfHashes = dis.readInt();
            this.probRate = dis.readDouble();
            int dataLength = dis.readInt();
            long[] data = new long[dataLength];
            for (int i = 0; i < dataLength; i++) {
                data[i] = dis.readLong();
            }
            this.data = new AtomicLongArray(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Will try to read data from the input stream to constrcut a new bloomFilter from
     * * 0 byte -> k (numberOfHashes)
     * * 1 - 4 byte -> p (probRate)
     * * 5 - x byte -> data as utf8
     *
     * @param inputStream
     * @throws IOException
     */
    public void readFrom(InputStream inputStream) {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        this.readFromStream(dataInputStream);
    }
    //endregion

    //region Utility

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * Creates and returns a copy of this object.  The precise meaning
     * of "copy" may depend on the class of the object.
     *
     * @return a clone of this instance.
     * @throws CloneNotSupportedException if the object's class does not
     *                                    support the {@code Cloneable} interface. Subclasses
     *                                    that override the {@code clone} method can also
     *                                    throw this exception to indicate that an instance cannot
     *                                    be cloned.
     * @see Cloneable
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int hashCode() {
        return Objects.hashCode(new Object[]{this.numBits, this.numberOfHashes, this.getData()});
    }

    private void readObject(
            ObjectInputStream inputStream
    ) throws ClassNotFoundException, IOException {
        inputStream.defaultReadObject();
    }

    private void writeObject(
            ObjectOutputStream outputStream
    ) throws IOException {
        outputStream.defaultWriteObject();
    }
    //endregion

}