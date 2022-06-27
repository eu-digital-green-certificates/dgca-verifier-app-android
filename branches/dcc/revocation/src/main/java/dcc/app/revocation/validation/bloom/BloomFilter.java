/*
 * Copyright (c) 2022 T-Systems International GmbH and all other contributors
 * Author: Paul Ballmann
 */
package dcc.app.revocation.validation.bloom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import dcc.app.revocation.validation.bloom.exception.FilterException;

/**
 * To reduce the data privacy/memory footprint of provided UCIs, a bloom filter can be used to reduce the size of provided
 * revocation lists. The hash function used is SHA256. All other parameters can be selected by the participants. The generated
 * Bloom Filter is inserted in the partition as payload together with the associated parameters.
 * <p>
 * For bloom filters the associated parameters are the important point: it’s recommended to pack not more than 1000 Entries within one filter,
 * with an amount of around 20 hash functions, to reach an probability of false positives of around 1E-10.
 * The exact values and settings should be provided to the app to adjust within the verification all the time the calculation.
 */
public interface BloomFilter {

    float getP();

    int getK();

    long getM();

    int getN();

    void add(byte[] element) throws NoSuchAlgorithmException, FilterException, IOException;

    /**
     * Checks if element is inside of BloomFilter.
     *
     * @param element to check
     * @return true is contains false otherwise
     * @throws NoSuchAlgorithmException
     * @throws FilterException
     * @throws IOException
     */
    boolean mightContain(byte[] element) throws NoSuchAlgorithmException, FilterException, IOException;

    void readFrom(InputStream inputStream);

    void writeTo(OutputStream outputStream) throws FilterException, IOException;
}