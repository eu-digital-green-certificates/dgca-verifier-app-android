package dgca.verifier.app.decoder

import dgca.verifier.app.decoder.model.RecoveryStatement
import org.junit.Assert
import org.junit.Test

class CertificateCheckTest {
    @Test
    fun TestTestValidity() {
        val test = dgca.verifier.app.decoder.model.Test(
            "12",
            "",
            "",
            "",
            "2021-02-20T12:34:56Z",
            "",
            "260415000",
            "",
            "",
            "",
            ""
        )
        Assert.assertTrue(test.isDateInThePast())
        Assert.assertTrue(test.isResultNegative())
    }

    @Test
    fun testIsCertificateNotValidAnymore() {
        var recovery = RecoveryStatement("", "", "", "", "", "2021-03-04", "")
        Assert.assertTrue(recovery.isCertificateNotValidAnymore()!!)
        recovery = RecoveryStatement("", "", "", "", "", "2030-02-04", "")
        Assert.assertTrue(!recovery.isCertificateNotValidAnymore()!!)
        recovery = RecoveryStatement("", "", "", "", "", "2021-02-20T12:34:56Z", "")
        Assert.assertTrue(recovery.isCertificateNotValidAnymore()!!)
        recovery = RecoveryStatement("", "", "", "", "", "2007-12-03T10:15:30+01:00", "")
        Assert.assertTrue(recovery.isCertificateNotValidAnymore()!!)
    }

    @Test
    fun testIsCertificateNotValidSoFar() {
        var recovery = RecoveryStatement("", "", "", "", "2100-03-04", "", "")
        Assert.assertTrue(recovery.isCertificateNotValidSoFar()!!)
        recovery = RecoveryStatement("", "", "", "", "2000-02-04", "", "")
        Assert.assertTrue(!recovery.isCertificateNotValidSoFar()!!)
        recovery = RecoveryStatement("", "", "", "", "2100-02-20T12:34:56Z", "", "")
        Assert.assertTrue(recovery.isCertificateNotValidSoFar()!!)
        recovery = RecoveryStatement("", "", "", "", "2100-12-03T10:15:30+01:00", "", "")
        Assert.assertTrue(recovery.isCertificateNotValidSoFar()!!)
    }

    @Test
    fun testCertificateValidity() {
        val recovery = RecoveryStatement("", "", "", "", "", "", "")
        Assert.assertNull(recovery.isCertificateNotValidSoFar())
        Assert.assertNull(recovery.isCertificateNotValidAnymore())
    }
}