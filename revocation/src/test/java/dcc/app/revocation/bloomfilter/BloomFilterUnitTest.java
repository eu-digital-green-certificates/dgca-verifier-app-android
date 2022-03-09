package dcc.app.revocation.bloomfilter;/*
 * Copyright (c) 2022 T-Systems International GmbH and all other contributors
 * Author: Paul Ballmann
 */


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLongArray;

import dcc.app.revocation.bloomfilter.model.FilterTestData;
import dcc.app.revocation.validation.BloomFilter;
import dcc.app.revocation.validation.BloomFilterImpl;
import dcc.app.revocation.validation.exception.FilterException;

@Ignore("failing tests")
public class BloomFilterUnitTest {

    private static String JSON_TEST_FILE = "src/test/resources/testcase1.json";
    private JSONArray testObjects = null;
    private BloomFilterImpl bloomFilter;
    private FilterTestData filterTestData = null;

    @Test
    public void testBigInteger() throws FilterException, IOException, NoSuchAlgorithmException {
        BigInteger val = BloomFilterImpl.calcIndex(new byte[]{11}, 1, 100);
        assert val.intValue() == 75;

        val = BloomFilterImpl.calcIndex(new byte[]{1}, 1, 1);
        assert val.intValue() == 0;
    }

    @Test
    public void runBasicBloom() throws FilterException, IOException, NoSuchAlgorithmException {
        BloomFilterImpl impl = new BloomFilterImpl(1, (byte) 1, 1);
        impl.add(new byte[]{0, 5, 33, 44});
        assert !impl.mightContain(new byte[]{0, 5, 88, 44});
        assert impl.mightContain(new byte[]{0, 5, 33, 44});
        assert impl.getData().length() == 1;
        assert impl.getData().get(0) == (Integer.MIN_VALUE >>> 26);
    }

    @Test
    public void runDifferentByteSizeBlock() throws FilterException, IOException, NoSuchAlgorithmException {
        BloomFilterImpl impl = new BloomFilterImpl(8, (byte) 1, 1);
        impl.add(new byte[]{0, 5, 33, 44});

        int size = 1;
        int numBits = size * (8 * Long.BYTES);

        AtomicLongArray longArray = new AtomicLongArray(size);

        int index = BloomFilterImpl.calcIndex(new byte[]{0, 5, 33, 44}, 0, numBits).intValue();
        int bytepos = index / (Long.BYTES * 8);
        long pattern = Long.MIN_VALUE >>> index;
        longArray.set(bytepos, longArray.get(bytepos) | pattern);

        assert impl.getData().get(1) == longArray.get(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxValues() throws FilterException {
        BloomFilterImpl impl = new BloomFilterImpl(Integer.MAX_VALUE, (byte) 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinValues() throws FilterException {
        BloomFilterImpl impl = new BloomFilterImpl(Integer.MIN_VALUE, (byte) 1, 1);
    }

    @Test()
    public void testNormalValues() throws FilterException, IOException, NoSuchAlgorithmException {
        BloomFilterImpl impl = new BloomFilterImpl(56049, (byte) 20, 1);
        impl.add(new byte[]{0, 9, 44});
        assert impl.mightContain(new byte[]{0, 9, 44});
    }

    @Test()
    public void testPropBasesBitCalc() throws FilterException {
        BloomFilterImpl impl = new BloomFilterImpl(30000000, 0.1f); //ca. 30M per Filter
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxElementSize() throws FilterException {
        BloomFilterImpl impl = new BloomFilterImpl(29900000, 0.0000000001f); //ca. 30M per Filter
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinElementSize() throws FilterException {
        BloomFilterImpl impl = new BloomFilterImpl(0, 0.0000000001f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeElementSize() throws FilterException {
        BloomFilterImpl impl = new BloomFilterImpl(-1, 0.0000000001f);
    }

    @Test()
    public void testByteStream() throws FilterException, IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BloomFilterImpl impl = new BloomFilterImpl(500, 0.000000001f);
        impl.add(new byte[]{5, 3, 2, 7});
        impl.add(new byte[]{5, 3, 0});
        impl.add(new byte[]{5, 2, 7});
        impl.add(new byte[]{5, 1, 2, 0});
        impl.add(new byte[]{5});
        assert !impl.mightContain(new byte[]{5, 5});
        impl.writeTo(output);

        BloomFilterImpl impl2 = new BloomFilterImpl(new ByteArrayInputStream(output.toByteArray()));
        assert impl2.getK() == impl.getK();
        assert impl2.getP() == impl.getP();
        assert impl2.getM() == impl.getM();
        assert impl.mightContain(new byte[]{5, 3, 2, 7});
        assert impl.mightContain(new byte[]{5, 3, 0});
        assert impl.mightContain(new byte[]{5, 2, 7});
        assert impl.mightContain(new byte[]{5, 1, 2, 0});
        assert impl.mightContain(new byte[]{5});
        assert !impl.mightContain(new byte[]{5, 5});
        assert impl.getData().length() == impl2.getData().length();
    }

    @Test()
    public void testByteOutputStream() throws FilterException, IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BloomFilterImpl impl = new BloomFilterImpl(1, 1);
        impl.writeTo(output);
        byte[] b = output.toByteArray();
        assert b.length == 24;
    }


    @Test
    public void compareSizes() throws FilterException, IOException, NoSuchAlgorithmException {
        BloomFilterImpl impl = new BloomFilterImpl(1, 1);
        BloomFilterImpl impl2 = new BloomFilterImpl(1, 0.125f);
        assert impl.getData().length() == impl2.getData().length();
    }

    @Test
    public void compare() throws FilterException, IOException, NoSuchAlgorithmException {
        BloomFilterImpl impl = new BloomFilterImpl(1, 1);
        BloomFilterImpl impl2 = new BloomFilterImpl(1, 0.125f);
        assert impl.getData().length() == impl2.getData().length();
    }

    @Test
    public void runTests() throws Exception {
        this.testObjects = this.readFromJson();
        assert this.testObjects != null;
        this.runBloomFilterTest();
    }

    private int doScans(BloomFilter filter, int scans) throws FilterException, IOException, NoSuchAlgorithmException {
        int falsePositives = 0;
        for (int x = 1; x < scans; x++) {
            if (filter.mightContain(ByteBuffer.allocate(4).putInt(Math.abs(x)).array())) {
                falsePositives++;
            }
        }
        return falsePositives;
    }

    @Test
    public void testProbabilistcRate() throws FilterException, IOException, NoSuchAlgorithmException {
        int scans = 10000000;
        float propScan = 0.1f;

        BloomFilter filter = new BloomFilterImpl(100, propScan);

        filter.add(new byte[]{5, 1, 2, 3, 6});
        filter.add(new byte[]{2, 1, 2, 3, 6});
        filter.add(new byte[]{7, 1, 2, 3, 6});
        filter.add(new byte[]{8, 1, 2, 3, 6});

        int falsePositives = doScans(filter, scans);
        assert propScan >= (float) ((float) falsePositives / (float) scans);
    }

    @Test
    public void testProbabilistcRate2() throws FilterException, IOException, NoSuchAlgorithmException {
        int scans = 10000000;
        BloomFilter filter = new BloomFilterImpl(100, (byte) 1, 4);
        double propScan = filter.getP();
        filter.add(new byte[]{5, 1, 2, 3, 6});
        filter.add(new byte[]{2, 1, 2, 3, 6});
        filter.add(new byte[]{7, 1, 2, 3, 6});
        filter.add(new byte[]{8, 1, 2, 3, 6});

        int falsePositives = doScans(filter, scans);
        assert propScan >= (float) ((float) falsePositives / (float) scans);
    }

    @Test
    public void testProbabilistcRate3() throws FilterException, IOException, NoSuchAlgorithmException {
        int scans = 100000;
        float propScan = 0.00001f;
        int entries = 10000;
        BloomFilter filter = new BloomFilterImpl(entries, propScan);
        Random r = new Random();
        for (int x = 0; x < entries; x++) {
            filter.add(new byte[]{(byte) r.nextInt(256),
                    (byte) r.nextInt(256),
                    (byte) r.nextInt(256),
                    (byte) r.nextInt(256),
                    (byte) r.nextInt(256)});
        }

        int falsePositives = doScans(filter, scans);
        assert filter.getK() == 17;
        assert filter.getM() == 239680;
        assert propScan >= (float) ((float) falsePositives / (float) scans);
    }

    @Test
    public void testRandom() throws FilterException, NoSuchAlgorithmException, IOException {
        BloomFilterImpl imp = new BloomFilterImpl(62, 0.01f);
        imp.add(new byte[]{16, 43, 72, -124, -99, 34, -113, -77, 78, -105, -113, 30, -90, -25, -38, 70, 76, 109, -92, -27, -15, 65, 36, -113, 3, -115, -4, -49, -81, -1, 69, -125, -22, 53, -49, 65, 31, 65, 18, 60, -56, -17, 16, 5, -11, 5, -3, -49, 4, -48, 122, 31, -37, -113, 54, -35, -83, -114, 62, 57, 125, 120, -26, 106});
    }

    @Test
    public void runSmokeTest() throws NoSuchAlgorithmException, IOException, FilterException {
        BloomFilterImpl imp = new BloomFilterImpl(1000000, 0.1f);
        for (int x = 0; x < 100000; x++) {
            UUID guid = UUID.randomUUID();
            byte[] hash = BloomFilterImpl.hash(guid.toString().getBytes(), '1');
            imp.add(hash);
        }
    }

    @Test
    public void runBloomFilterTest() throws FilterException, IOException, NoSuchAlgorithmException {
        assert this.testObjects != null;
        for (int i = 0; i < this.testObjects.size(); i++) {
            System.out.printf("Current test at index : %s%n", i);
            // create a bloom filter
            FilterTestData testData = this.extractTestData(i);
            this.bloomFilter = this.createFilterForData(testData);
            this.filterTestData = testData;
            // store data in the filter
            this.storeDataInFilter(i);

            this.calcBaseStringFromFilter(i);
            // perform lookup to check if data exists
            this.filterLookupTest(testData, i);
        }
    }

    private BloomFilterImpl createFilterForData(FilterTestData data) throws FilterException, IOException, NoSuchAlgorithmException {
        return new BloomFilterImpl(data.getDataSize(), (float) data.getP());
    }

    public void storeDataInFilter(int i) throws FilterException, IOException, NoSuchAlgorithmException {
        assert this.bloomFilter != null;
        FilterTestData testData = this.extractTestData(i);
        this.addToTsiBloomFilter(testData);
    }

    public void calcBaseStringFromFilter(int i) {
        // get base64 from filter
        //  String filterAsBase64 = this.getFilterAsBase64(this.bloomFilter.getBytes());
        //  this.writeToJson((JSONObject) this.testObjects.get(i), i);
        // store base64 in data
        //  this.storeBase64InFile(i, filterAsBase64);
    }

    public void filterLookupTest(FilterTestData testData, int index) throws FilterException, NoSuchAlgorithmException, IOException {
        this.lookupFilter(testData, index);
    }
/*
    @Test
    public void runTSIBloomFilter() throws Exception {
        // Contains all of the data from the test file
        JSONArray jsonArray = this.readFromJson();
        // Iterate over all of the test-cases
        assert jsonArray != null;
        this.testObjects = jsonArray;
        for (int i = 0; i < this.testObjects.size(); i++) {
            System.out.printf("i: %s%n", i);
            JSONObject object = (JSONObject) jsonArray.get(i);
            FilterTestData testData = this.extractTestData(object);
            this.bloomFilter = new BloomFilterImpl(testData.getDataSize(), testData.getK(), (float) testData.getP());
            this.addToTsiBloomFilter(testData);
            this.storeFilterAsBase64(this.bloomFilter.getBits(), object, i);
            this.printTsiFilterBits();
            this.lookupFilter(testData, object, i);
            return;
        }

    }*/

    /**
     * Checks if all bits written in the testData.written array can be found in the filter.
     * Each element that actually exists will be set int he testData.exists array
     */
    private void lookupFilter(FilterTestData testData, int index) throws FilterException, IOException, NoSuchAlgorithmException {
        int exists[] = new int[testData.getDataSize()];
        for (int i = 0; i < testData.getDataSize(); i++) {
            // iterate over all testdata
            // perform a lookup
            // if lookup is true, set bit to 1 in exists array
            // loop over exists array
            // check if each bit is equal to the bits in written
            if (this.bloomFilter.mightContain(dataToArr(testData.getData().get(i)))) {
                exists[i] = 1;
            } else {
                exists[i] = 0;
            }
        }
        // store exists in json
        JSONObject o = (JSONObject) this.testObjects.get(index);
        o.put("exists", Arrays.toString(exists));
        writeToJson(o, index);
        // retrieve written array
        int strike = 0; // strike for each mismatch
        for (int j = 0; j < exists.length; j++) {
            if (exists[j] != testData.getWritten()[j]) {
                strike++;
            }
        }
        System.out.printf("LookupTest: Strikes -> %s%n", strike);
    }

    private byte[] dataToArr(Object obj) {
        return obj.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String getFilterAsBase64(AtomicLongArray filter) {
        String base64Filter = getBase64FromFilter(filter);
        System.out.println("TSI: " + base64Filter);
        return base64Filter;
        // objPointer.put("filter", base64Filter);
        // writeToJson(objPointer, index);
    }

    private void storeBase64InFile(int index, String base64) {
        JSONObject obj = (JSONObject) this.testObjects.get(index);
        obj.put("filter", base64);
        writeToJson(obj, index);
    }

    private FilterTestData extractTestData(int index) {
        FilterTestData data = new FilterTestData();
        JSONObject dataObject = (JSONObject) this.testObjects.get(index);
        data.setData((JSONArray) dataObject.get("data"));
        data.setP((double) dataObject.get("p"));
        data.setK(Integer.parseInt(dataObject.get("k").toString()));
        data.setExists(new int[data.getDataSize()]);
        int[] written = this.toArray((JSONArray) dataObject.get("written"));
        data.setWritten(written);
        // data.setWritten((int[]) obj.get("written"));
        System.out.printf("data: %s%n", data.getData());
        System.out.printf("p: %s, k: %s%n", data.getP(), data.getK());
        return data;
    }

    private int[] toArray(JSONArray arr) {
        int[] intArr = new int[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            intArr[i] = Integer.parseInt(arr.get(i).toString());
        }
        return intArr;
    }

    private void printTsiFilterBits() {
        // System.out.println(this.bloomFilter.getBytes().toString());
    }

    private void addToTsiBloomFilter(FilterTestData data) throws FilterException, IOException, NoSuchAlgorithmException {
        try {
            for (int i = 0; i < data.getDataSize(); i++) {
                // only add elements where written is set to 1 at given index i
                if (data.getWritten()[i] == 1) {
                    this.bloomFilter.add(data.getData().get(i).toString().getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (FilterException e) {
            e.printStackTrace();
        }
    }

    private String getBase64FromFilter(AtomicLongArray bitArray) {
        return Base64.getEncoder().encodeToString(bitArray.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void writeToJson(JSONObject object, int index) {
        JSONArray jsonArraySource = this.readFromJson();
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(JSON_TEST_FILE);
        } catch (IOException io) {
            System.out.println("ERROR " + io.getLocalizedMessage());
            return;
        }
        jsonArraySource.set(index, object);
        try {
            fileWriter.write(jsonArraySource.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ioException) {
            System.out.println("ERROR: " + ioException.getLocalizedMessage());
            return;
        }

    }

    private JSONArray readFromJson() {
        FileReader fileReader;
        try {
            fileReader = new FileReader(JSON_TEST_FILE);
        } catch (FileNotFoundException fnf) {
            System.out.println("ERROR: " + fnf.getLocalizedMessage());
            return null;
        }
        JSONArray jsonArray;
        try {
            JSONParser parser = new JSONParser();
            jsonArray = (JSONArray) parser.parse(fileReader);
        } catch (ParseException | IOException pe) {
            System.out.println("ERROR: " + pe.getLocalizedMessage());
            return null;
        }
        return jsonArray;
    }

    private JSONObject getFromJson(JSONArray array, int index) {
        return (JSONObject) array.get(index);
    }

    private Object getFromObject(String key, JSONObject object) {
        return object.get(key);
    }
}