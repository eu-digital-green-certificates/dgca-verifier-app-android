/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
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
 *  Created by SchulzeStTSI on 22.06.21, 00:13
 */

package dgca.verifier.app.android;

import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import dgca.verifier.app.android.verification.VerificationError;
import dgca.verifier.app.android.verification.VerificationViewModel;
import dgca.verifier.app.decoder.model.GreenCertificate;
import dgca.verifier.app.decoder.model.Person;
import dgca.verifier.app.decoder.model.RecoveryStatement;
import dgca.verifier.app.decoder.model.RecoveryVerificationResult;
import dgca.verifier.app.decoder.model.VerificationResult;
import dgca.verifier.app.android.verification.VerificationErrorKt;

public class CertificateTests {

    @Test
    public void TestRecoveryValidity()
    {
        VerificationResult result = new VerificationResult();
        result.setBase45Decoded(true);
        result.setCborDecoded(true);
        result.setContextPrefix("HC1");
        result.setCoseVerified(true);
        result.setIssuedTimeCorrect(true);
        result.setSchemaValid(true);
        result.setNotExpired(true);
        result.setZlibDecoded(true);


        List<RecoveryStatement> statement = new ArrayList<RecoveryStatement>();
        statement.add(new RecoveryStatement("","","","","","2020-06-01",""));

        GreenCertificate certificate = new GreenCertificate("1.0.0",
                                                            new Person("HORST",
                                                                    "DIETER",
                                                                    "CLAUS",
                                                                    "TEST"),
                                                                "2012-01-01",null,null,statement);

        VerificationViewModel.Companion.validateCertData(certificate,result);

        Assert.assertFalse(result.isValid());
        Assert.assertTrue(result.getRecoveryVerification().isRecoveryDateInThePast());
        statement.clear();
        statement.add(new RecoveryStatement("","","","","","2097-06-01",""));

        certificate = new GreenCertificate("1.0.0",
                new Person("HORST",
                        "DIETER",
                        "CLAUS",
                        "TEST"),
                "2060-01-01",null,null,statement);

        VerificationViewModel.Companion.validateCertData(certificate,result);

        Assert.assertTrue(result.isValid());
        Assert.assertFalse(result.getRecoveryVerification().isRecoveryDateInThePast());
    }

    @Test
    public void TestNoPublicKeysFound()
    {
        VerificationResult result = new VerificationResult();
        VerificationError error= dgca.verifier.app.android.verification.VerificationErrorKt.fetchError(result,true);
        Assert.assertTrue(error == VerificationError.VERIFICATION_FAILED);
    }

    @Test
    public void TestCertExpired()
    {
        VerificationResult result = new VerificationResult();
        result.setCoseVerified(true);
        result.setNotExpired(false);
        VerificationError error= dgca.verifier.app.android.verification.VerificationErrorKt.fetchError(result,false);

        Assert.assertTrue(error == VerificationError.CERTIFICATE_EXPIRED);
    }

    @Test
    public void TestSignatureInvalid()
    {
        VerificationResult result = new VerificationResult();
        result.setNotExpired(false);
        result.setCoseVerified(false);
        VerificationError error= dgca.verifier.app.android.verification.VerificationErrorKt.fetchError(result,false);

        Assert.assertTrue(error == VerificationError.CRYPTOGRAPHIC_SIGNATURE_INVALID);
    }

    @Test
    public void TestVerificationFailed()
    {
        VerificationResult result = new VerificationResult();
        result.setNotExpired(true);
        result.setCoseVerified(true);
        result.setBase45Decoded(false);
        VerificationError error= dgca.verifier.app.android.verification.VerificationErrorKt.fetchError(result,false);

        Assert.assertTrue(error == VerificationError.VERIFICATION_FAILED);

        result.setNotExpired(true);
        result.setCoseVerified(true);
        result.setBase45Decoded(true);
        result.setCborDecoded(false);
        error= dgca.verifier.app.android.verification.VerificationErrorKt.fetchError(result,false);

        Assert.assertTrue(error == VerificationError.VERIFICATION_FAILED);

        result.setNotExpired(true);
        result.setCoseVerified(true);
        result.setBase45Decoded(true);
        result.setCborDecoded(true);
        result.setSchemaValid(false);
        error= dgca.verifier.app.android.verification.VerificationErrorKt.fetchError(result,false);

        Assert.assertTrue(error == VerificationError.VERIFICATION_FAILED);
    }

    @Test
    public void TestRecoveryExpiration()
    {
        VerificationResult result = new VerificationResult();
        result.setNotExpired(true);
        result.setCoseVerified(true);
        result.setRecoveryVerification(new RecoveryVerificationResult(true));

        VerificationError error= dgca.verifier.app.android.verification.VerificationErrorKt.fetchError(result,false);

        Assert.assertTrue(error == VerificationError.CERTIFICATE_EXPIRED);
    }
}
