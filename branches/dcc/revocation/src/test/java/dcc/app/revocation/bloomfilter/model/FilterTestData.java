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

    private JSONArray data;   // data
    private double p;         // false positive rate
    private int k;            // number of hashes
    private int[] written;    // bit representation of which data has been written

    public FilterTestData() {
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        this.data = data;
    }

    public int getDataSize() {
        return this.data.size();
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        if (p == 0) {
            throw new IllegalArgumentException();
        }
        this.p = p;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        if (k == 0) {
            throw new IllegalArgumentException();
        }
        this.k = k;
    }

    public int[] getWritten() {
        return written;
    }

    public void setWritten(int[] written) {
        if (written == null || written.length == 0) {
            throw new IllegalArgumentException();
        }
        this.written = written;
    }
}
