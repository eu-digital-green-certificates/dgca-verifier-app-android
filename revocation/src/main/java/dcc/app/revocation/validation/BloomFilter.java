/*
 * Copyright (c) 2022 T-Systems International GmbH and all other contributors
 * Author: Paul Ballmann
 */

package dcc.app.revocation.validation;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface BloomFilter {

    void add(byte[] element) throws NoSuchAlgorithmException, IOException;

    boolean contains(byte[] element) throws NoSuchAlgorithmException, IOException;
}