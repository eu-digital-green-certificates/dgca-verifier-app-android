/*
 * Copyright (c) 2022 T-Systems International GmbH and all other contributors
 * Author: Paul Ballmann
 */

package dcc.app.revocation.validation;

import java.io.InputStream;
import java.io.OutputStream;

import dcc.app.revocation.validation.exception.FilterException;


public interface BloomFilter {
    double getP();

    int getK();

    long getM();

    int getN();

    int getCurrentN();

    void add(byte[] element) throws FilterException;

    boolean mightContain(byte[] element) throws FilterException;

    void readFrom(InputStream inputStream);

    void writeTo(OutputStream outputStream) throws FilterException;

    void reset(int bytes);

}
