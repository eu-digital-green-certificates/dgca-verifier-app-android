/*
 * Copyright (c) 2022 T-Systems International GmbH and all other contributors
 * Author: Paul Ballmann
 */

package dcc.app.revocation.bloomfilter.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Wrapper class of all supported hash functions.
 * Names will be passed into MessageDigest.getInstance(name).
 */
public enum HashFuncEnum {
    SHA1("SHA-1"),
    SHA256("SHA-256"),
    MD5("MD5");

    private String hashFunctionName;

    HashFuncEnum(String hashName) {
        this.hashFunctionName = hashName;
    }

    public String getHashFunctionName() {
        return this.hashFunctionName;
    }

    public static Optional<HashFuncEnum> get(String name) {
        return Arrays.stream(HashFuncEnum.values())
                .filter(hashName -> hashName.hashFunctionName.equals(name))
                .findFirst();
    }
}
