/*
 * Copyright (c) 2022 T-Systems International GmbH and all other contributors
 * Author: Paul Ballmann
 */

package dcc.app.revocation.bloomfilter.model;

import org.json.simple.JSONArray;

/**
 * Class to hold all data received from the json test files
 */
public class FilterTestData {
    //region Fields
    private JSONArray data;             // data
    private double p;                // false positive rate
    private int k;                // number of hashes
    private HashFuncEnum hashFunction;     // name of the hash function to use
    private String filterAsBase64;    // the filter as base64 string
    private int[] written;          // bit representation of which data has been written
    private int[] exists;           // bit rep of data that exists in data
    private boolean[] rawBit;           // raw bit rep of array
    //endregion

    //region Constructor
    public FilterTestData() {
    }
    //endregion

    //region Getter & Setter
    public JSONArray getData() {
        return data;
    }

    public FilterTestData setData(JSONArray data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        this.data = data;
        return this;
    }

    public int getDataSize() {
        return this.data.size();
    }

    public double getP() {
        return p;
    }

    public FilterTestData setP(double p) {
        if (p == 0) {
            throw new IllegalArgumentException();
        }
        this.p = p;
        return this;
    }

    public int getK() {
        return k;
    }

    public FilterTestData setK(int k) {
        if (k == 0) {
            throw new IllegalArgumentException();
        }
        this.k = k;
        return this;
    }

    public HashFuncEnum getHashFunction() {
        return hashFunction;
    }

    public FilterTestData setHashFunction(HashFuncEnum hashFunction) {
        if (hashFunction == null) {
            throw new IllegalArgumentException();
        }
        this.hashFunction = hashFunction;
        return this;
    }

    public String getFilterAsBase64() {
        return filterAsBase64;
    }

    public FilterTestData setFilterAsBase64(String filterAsBase64) {
        if (filterAsBase64 == null || filterAsBase64.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.filterAsBase64 = filterAsBase64;
        return this;
    }

    public int[] getWritten() {
        return written;
    }

    public FilterTestData setWritten(int[] written) {
        if (written == null || written.length == 0) {
            throw new IllegalArgumentException();
        }
        this.written = written;
        return this;
    }

    public int[] getExists() {
        return exists;
    }

    public FilterTestData setExists(int[] exists) {
        if (exists == null) {
            throw new IllegalArgumentException();
        }
        this.exists = exists;
        return this;
    }

    public boolean[] getRawBit() {
        return rawBit;
    }

    public FilterTestData setRawBit(boolean[] rawBit) {
        if (rawBit == null) {
            throw new IllegalArgumentException();
        }
        this.rawBit = rawBit;
        return this;
    }
    //endregion
}
