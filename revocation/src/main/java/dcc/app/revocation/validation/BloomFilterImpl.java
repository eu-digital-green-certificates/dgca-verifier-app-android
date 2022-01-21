/*
 * Copyright (c) 2022 T-Systems International GmbH and all other contributors
 * Author: Paul Ballmann
 */

package dcc.app.revocation.validation;

import com.google.common.primitives.SignedBytes;
import com.google.common.primitives.UnsignedBytes;

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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongArray;

public class BloomFilterImpl implements BloomFilter, Serializable {
    private int numBytes;
    private int numberOfHashes;
    private float probRate;
    private AtomicLongArray bits;
    private final static int NUM_BITS = 8;
    private final static int DATA_OFFSET = 6;
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
        this.numBytes = (size * NUM_BITS);
        this.numberOfHashes = numberOfHashes;
        // p = pow(1 - exp(-k / (m / n)), k)
        this.probRate = (float) Math.pow(1 - Math.exp(-numberOfHashes / ((this.numBytes / NUM_BITS) / this.numBytes)), numberOfHashes);
        this.bits = new AtomicLongArray(this.numBytes);
    }

    public BloomFilterImpl(int numberOfElements, int numberOfHashes, float probRate) {
        super();
        if (numberOfElements == 0 || numberOfHashes == 0 || probRate > 1 || probRate == 0) {
            throw new IllegalArgumentException("numberOfElements != 0, numberOfHashes != 0, probRate <= 1");
        }
        // n: numberOfElements
        // m: numberOfBits -> ceil((n * log(p)) / log(1 / pow(2, log(2))));
        this.numBytes = (int) (Math.ceil((numberOfElements * Math.log(probRate)) / Math.log(1 / Math.pow(2, Math.log(2)))));
        this.numberOfHashes = numberOfHashes;
        this.probRate = probRate;
        this.bits = new AtomicLongArray(this.numBytes);
    }

    public AtomicLongArray getBits() {
        return bits;
    }

    @Override
    public void add(byte[] element) throws NoSuchAlgorithmException, IOException {
        for (int i = 0; i < this.numberOfHashes; i++) {
            BigInteger index = this.calcInternal(element, i);
            System.out.println("INDEX: " + index);
            this.bits.set(index.intValue(), 0x1);
        }
    }

    @Override
    public boolean contains(byte[] element) throws NoSuchAlgorithmException, IOException {
        for (int i = 0; i < this.numberOfHashes; i++) {
            BigInteger index = this.calcInternal(element, i);
            if (this.bits.get(index.intValue()) == 0x1) {
                return true;
            }
        }
        return false;
    }

    private BigInteger calcInternal(byte[] element, int i) throws NoSuchAlgorithmException, IOException {
        BigInteger bi = new BigInteger(this.hash(element, (char) i));
        return bi.mod(BigInteger.valueOf(this.numBytes));
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

        dataOutputStream.write(new byte[]{UnsignedBytes.checkedCast(this.numberOfHashes)}, 0, 1);
        dataOutputStream.write(new byte[]{SignedBytes.checkedCast((long) this.probRate)}, 1, 4);
        dataOutputStream.write(new byte[]{UnsignedBytes.checkedCast(this.getBits().length())}, 5, 1);
        // dataOutputStream.writeInt(this.numberOfHashes);
        // dataOutputStream.write(UnsignedBytes.checkedCast(((long) this.probRate)));
        // dataOutputStream.write(this.getBits().toString().getBytes(StandardCharsets.UTF_8));
        dataOutputStream.write(this.getBits().toString().getBytes(StandardCharsets.UTF_8),
                DATA_OFFSET, this.getBits().length());
        /*
        for (int i = 0; i < this.getBits().length(); i++) {
            dataOutputStream.writeLong(this.getBits().get(i));
        }
        */
    }

    private void readFromStream(DataInputStream dis) {
        try {
            this.numberOfHashes = dis.read(new byte[1]);
            this.probRate = dis.read(new byte[4]);
            int dataLength = dis.read(new byte[1]);
            long[] data = new long[dataLength];
            for (int i = 0; i < dataLength; i++) {
                data[i] = dis.read();
            }
            this.bits = new AtomicLongArray(data);
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
        return Objects.hashCode(new Object[]{this.numBytes, this.numberOfHashes, this.getBits()});
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