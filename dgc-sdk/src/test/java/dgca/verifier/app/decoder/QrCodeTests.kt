package dgca.verifier.app.decoder

import com.fasterxml.jackson.databind.ObjectMapper
import dgca.verifier.app.decoder.base45.Base45Decoder
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.base45.DefaultBase45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.cbor.DefaultCborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.compression.DefaultCompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.cose.CryptoService
import dgca.verifier.app.decoder.cose.DefaultCoseService
import dgca.verifier.app.decoder.cose.VerificationCryptoService
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.decoder.prefixvalidation.DefaultPrefixValidationService
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.DefaultSchemaValidator
import dgca.verifier.app.decoder.schema.SchemaValidator
import dgca.verifier.app.decoder.services.X509
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

class QrCodeTests {
    @Throws(CertificateException::class)
    fun toCertificate(pubKey: String?): X509Certificate {
        val `in` = Base64.getDecoder().decode(pubKey)
        val inputStream: InputStream = ByteArrayInputStream(`in`)
        return CertificateFactory.getInstance("X.509")
            .generateCertificate(inputStream) as X509Certificate
    }

    fun verify(prefix: String?, PublicKey: String?): Boolean {
        val result = VerificationResult()
        val b45Service: Base45Service = DefaultBase45Service()
        val prefService: PrefixValidationService = DefaultPrefixValidationService()
        val compressorService: CompressorService = DefaultCompressorService()
        val validator: SchemaValidator = DefaultSchemaValidator()
        val coseservice: CoseService = DefaultCoseService()
        val cborservice: CborService = DefaultCborService()
        val base45 = prefService.decode(prefix!!, result)
        val compressed = b45Service.decode(base45, result)
        val cose = compressorService.decode(compressed, result)
        val cbor = coseservice.decode(cose, result)
        val greenCertificate = cborservice.decode(cbor!!.cbor, result)
        val schemaresult = validator.validate(cbor.cbor, result)
        val cryptoService: CryptoService = VerificationCryptoService(X509())
        try {
            val cert: X509Certificate = toCertificate(PublicKey)
            cryptoService.validate(cose, cert, result, greenCertificate!!.getType())
        } catch (ex: Exception) {
            return false
        }
        return result.isValid()
    }

    @Test
    fun testPLCode() {
        val hCert =
            "HC1:6BFOXN%TS3DH1QG9WA6H98BRPRHO DJS4F3S-%2LXKQGLAVDQ81LO2-36/X0X6BMF6.UCOMIN6R%E5UX4795:/6N9R%EPXCROGO3HOWGOKEQBKL/645YPL\$R-ROM47E.K6K8I115DL-9C1QD+82D8C+ CH8CV9CA\$DPN0NTICZU80LZW4Z*AK.GNNVR*G0C7PHBO33/X086BTTTCNB*UJHMJ8J3HONNQN09B5PNVNNWGJZ730DNHMJSLJ*E3G23B/S7-SN2H N37J3 QTULJ7CB3ZC6.27AL4%IY.IQH5YRT5*K51T 1DT 456L X4CZKHKB-43.E3KD3OAJ/9TL4T1C9 UP IPGTUI7FKQU2N1L8VFLU9WU.B9 UPYR181A0+P8V7/JA--J/XTQWE/PEBLEH-BY.CECH$6KJEM*PC9JAU-BZ8ERJCS0DUMQI+O1-ST*QGTA4W7.Y7G+SB.V Q5NN9TJ1TM8554.8EW E2NS6F9\$J3-MQPSUB*H1EI+TUN73 39EX4165ABSXFB487V*K9J8UJC08H3N7T:DAIJC8K8T3TCF*6P.OB9Q721UJ+K.OJ4EW/S1*13PNG"
        val base45Decoder = Base45Decoder()
        val decoder = DefaultCertificateDecoder(base45Decoder)
        val result = decoder.decodeCertificate(hCert)
        assertTrue(result is CertificateDecodingResult.Success)
        val pubkey =
            "MIICnDCCAkKgAwIBAgIIJr8oA/3jYAQwCgYIKoZIzj0EAwIwUDEkMCIGA1UEAwwbUG9sYW5kIERHQyBSb290Q1NDQSAxIEFDQyBTMRswGQYDVQQKDBJNaW5pc3RyeSBvZiBIZWFsdGgxCzAJBgNVBAYTAlBMMB4XDTIxMDUyNDExMTgxNloXDTIzMDUyNDExMTgxNlowcjEtMCsGA1UEAwwkUG9sYW5kIFZhY2NpbmF0aW9uIERHQyBTZXJ2aWNlIDMgQUNDMRcwFQYDVQQLDA5lSGVhbHRoIENlbnRlcjEbMBkGA1UECgwSTWluaXN0cnkgb2YgSGVhbHRoMQswCQYDVQQGEwJQTDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABBb5V0Rbo5Qc1yAVxRyXaLt/QjmI4WG3qsXf81WoH6L2Uf4oj5iGnAuem1TSotax+FUgvn+GbcUg7BTrL+ePAQSjgeMwgeAwHwYDVR0jBBgwFoAUqc15HwkAJgfQl/0DpjHxRVJ9E28wFgYDVR0lBA8wDQYLKwYBBAGON49lAQIwTAYDVR0fBEUwQzBBoD+gPYY7aHR0cDovL2FjYy1wMS5lemRyb3dpZS5nb3YucGwvY2NwMS9jcmwvREdDUm9vdENTQ0ExQUNDUy5jcmwwHQYDVR0OBBYEFAenLsHAhybxn8MjzWYLq+xrD8iYMCsGA1UdEAQkMCKADzIwMjEwNTI0MTExODE2WoEPMjAyMjA1MjQxMTE4MTZaMAsGA1UdDwQEAwIHgDAKBggqhkjOPQQDAgNIADBFAiEAw17oXs3K8q+VorcGq014/zCZAnxqRIQ6fCkHGCENJWQCIB3hvpk+NdLphX7aokerbhsF6xuJ7hT6DnD67SFgLI/9"
        assertTrue(verify(hCert, pubkey))
    }

    @Test
    fun testBgCode() {
        val hCert =
            "HC1:NCFOXN*TS0BI\$ZDYSHIAL*ECH 8S021091JDNDC3LE84DIJ9CIE7-78WA46VGOU:ZH6I1%4JF 2K%5PK9CZLEQ56SP.E5BQ95ZM3763LED6N%ZEXE6%HULAV**M82F93I6*6 %6PK9B/0MCIMMISVDG8C5DL-9C1QDW33C8C0U09B91*KEDC6J0GJ4JXGHHBIWB.80XUTKQS7DS2*N.SSBNKA.G.P6A8IM%OVNIA KZ*U0I1-I0*OC6H0UWM2NISGH*BSPRAFTI/T1A.PECGX%EN+P.Y0/9TL4T.B9GYPNIN:EWD QZQHU*PH86DROI%KXYNYKTKK1Y R/03YVBO7L.CCP7A+5S*T08JFHAIN95+Y5 P4KDO+*OH:7SA7G6MS/5U*O3DRE6P6/QVHPOVQJT5FT5D75W9AV88G64KE809KV+EYMOL61I/JTYJJP66IL/XCBJBJ3DJGOBIG2%5AM4T/JKATN5NN7TA9QB.PY38PMKIQJ8:P-TVS L\$W8LOAFXUWWLP-RO1E550%/OE5"
        val base45Decoder = Base45Decoder()
        val decoder = DefaultCertificateDecoder(base45Decoder)
        val result = decoder.decodeCertificate(hCert)
        assertTrue(result is CertificateDecodingResult.Success)
        val pubkey =
            "MIICpDCCAkugAwIBAgIUCQqeQIDhCUErUgTaGLQWtpazE0wwCgYIKoZIzj0EAwIwbDELMAkGA1UEBhMCQkcxGzAZBgNVBAoMEk1pbmlzdHJ5IG9mIEhlYWx0aDEiMCAGA1UECwwZSGVhbHRoIEluZm9ybWF0aW9uIFN5c3RlbTEcMBoGA1UEAwwTQnVsZ2FyaWEgREdDIENTQ0EgMTAeFw0yMTA1MTExMzM1NDFaFw0yMzA1MTExMzM1NDFaMHIxCzAJBgNVBAYTAkJHMQ4wDAYDVQQHDAVTb2ZpYTEbMBkGA1UECgwSTWluaXN0cnkgb2YgSGVhbHRoMSIwIAYDVQQLDBlIZWFsdGggSW5mb3JtYXRpb24gU3lzdGVtMRIwEAYDVQQDDAlER0MgRFNDIDEwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAATKS3U1ssyUkLU8/l+N4WLHBJtJv7EfhhHSCS4sIDmxC1IEvSDNeWGBNRAd0y4c2qvk3mggEWTvXl4EemFOI4LCo4HEMIHBMAwGA1UdEwEB/wQCMAAwLAYDVR0fBCUwIzAhoB+gHYYbaHR0cDovL2NybC5oaXMuYmcvY3NjYTEuY3JsMB8GA1UdIwQYMBaAFCquB6sY+uzcJ1Q7ebdy5EPK5zMLMB0GA1UdDgQWBBSZ1xpVCsU4Ccmz1cn4cK+Af0o3gTAOBgNVHQ8BAf8EBAMCB4AwMwYDVR0lBCwwKgYMKwYBBAEAjjePZQEBBgwrBgEEAQCON49lAQIGDCsGAQQBAI43j2UBAzAKBggqhkjOPQQDAgNHADBEAiAZG+XA04EByYpauBQIaGiv6Jy7Y/N7FTmYscaQ4NeKJwIga1u+9Pq8+63QeU6gsCkf+jIKppr58EQMA6UF1I11VDE="
        assertTrue(verify(hCert, pubkey))
    }

    @Test
    fun testDECode() {
        val hCert =
            "HC1:6BFR%BH:7*I0PS33NUA9HWP5PZ2CLJ*GH7WV-UNA1VZJKZ6HX.A/5R..9*CV6+LJ*F.UN7A2BT8B+6B897S69R48S1.R1VJO9Q1ZZO+CC\$A9%T5X7RI25A8S57D JK-PQ+JR*FDTW3+1EC1JXLOQ58+KFL49ZMENAO.YOWR75PAH0HD6AIHCPWHJTF.RJ*JCSKEHL1N31HWEO67KJH8TIX-B3QB-+9*LCU:C:P2QEEQ7KF\$V--4CW7JWILDWU%Q%IO0LAK70J\$KW2JW56.KO8E2RHPH60ILI8T0N/7OEPD7P3+3IH9VZIVWP.44FX87QH5I97ZK0MK8OIGC3 3CQ6WO+9P9ECRSV%72M4L65 KAVKE*YPRHSIF1 89*4NDZ7FU6:F6NPJ1PHL059BGBB1%/C/J91R75Z5I7CWV0TREWYSY8ULK5HWPGEP\$SI5B1$8HDOCH3JEBCL*8SE2AZT9SC+84JVGR39:2V*TR:KBW/4S:FK DOHF-1789MQ.18CV2C3YCN79OR176:1U:0CQVNGDJ0GUPO%CRT+QC/O$:D/WQY$3*5UR2M4YPFXK\$DH"
        val base45Decoder = Base45Decoder()
        val decoder = DefaultCertificateDecoder(base45Decoder)
        val result = decoder.decodeCertificate(hCert)
        assertTrue(result is CertificateDecodingResult.Success)
    }

    @Test
    fun testNOCode() {
        val hCert =
            "HC1:NCF780+80T9WTWGSLKC 4J9965QTH121L3LCFBB*A3*70M+9FN03DCZSJWY0JAC4+UD97TK0F90KECTHGWJC0FDVQ4AIA%G7X+AQB9746VG7W0AV+AWM96X6FCAJY8-F6846W%6V%60ZAKB7UPCBJCR1AFVC*70LVC6JD846Y96A464W5.A6+EDL8F9-98LE* CMEDM-DXC9 QE-ED8%EDZCX3E$34Z\$EXVD-NC%69AECAWE.JCBECB1A-:8$966469L6OF6VX6Q\$D.UDRYA 96NF6L/5SW6Y57KQEPD09WEQDD+Q6TW6FA7C466KCN9E%961A6DL6FA7D46JPCT3E5JDMA7346D463W5Z57..DX%DZJC7/DCWO3/DTVDD5D9-K3VCI3DU2DGECUGDK MLPCG/D2SDUWGR095Y8DWO0IAMPCG/DU2DRB8SE9VXI\$PC5\$CUZCZ$5Y$527B0DR-NGD9R696*KOX\$N3E5G-ER 5ZOHMLQW4O-1M1I0OHE1SVLZNT361*ED+E7ICER5-HMV*47OO$5J+%Q8KU7+G275H7TDX9R+GZWG"
        val base45Decoder = Base45Decoder()
        val decoder = DefaultCertificateDecoder(base45Decoder)
        val result = decoder.decodeCertificate(hCert)
        assertTrue(result is CertificateDecodingResult.Success)
        val pubkey =
            "MIICKTCCAc+gAwIBAgITewAAAB77yzK1mZYu7QAAAAAAHjAKBggqhkjOPQQDAjA/MQswCQYDVQQGEwJOTzEbMBkGA1UEChMSTm9yc2sgaGVsc2VuZXR0IFNGMRMwEQYDVQQDEwpDU0NBIE5PIHYxMB4XDTIxMDYwNzA1NTY0MloXDTIzMDYwNzA2MDY0MlowUjELMAkGA1UEBhMCTk8xLTArBgNVBAoTJE5vcndlZ2lhbiBJbnN0aXR1dGUgb2YgUHVibGljIEhlYWx0aDEUMBIGA1UEAxMLRFNDIEhOIEVVIDIwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAR0UprGbSmy5WsMAyb0GXbzemkLRvmUNswy1lBGavDjHW7CTYPd+7yG/OGaXetTnboH0jDJeL1vVQvOr12T4+teo4GWMIGTMA4GA1UdDwEB/wQEAwIHgDAzBgNVHSUELDAqBgwrBgEEAQCON49lAQEGDCsGAQQBAI43j2UBAgYMKwYBBAEAjjePZQEDMB0GA1UdDgQWBBT1z+dhLhI7/AUOAdFiK4oqzEAlrzAfBgNVHSMEGDAWgBRBY3L2ecPBcffxgRI2UhCjJQp0JzAMBgNVHRMBAf8EAjAAMAoGCCqGSM49BAMCA0gAMEUCIDnEDlot8V1hen18ra7Xjv2bGL1mdz7453ItRdx4ubllAiEAkZZKE14rprcfPW6lKcS+SwQr7IWCrMYb/nZdhecUAHM="
        assertTrue(verify(hCert, pubkey))
    }

    @Test
    fun testNormalCode() {
        val hCert =
            "HC1:NCFOXNEG2NBJ5*H:QO-.OMBN+XQ99N*6RFS5*TCVWBM*4ODMS0NSRHAL9.4I92P*AVAN9I6T5XH4PIQJAZGA2:UG%U:PI/E2$4JY/KB1TFTJ:0EPLNJ58G/1W-26ALD-I2\$VFVVE.80Z0 /KY.SKZC*0K5AFP7T/MV*MNY\$N.R6 7P45AHJSP\$I/XK\$M8TH1PZB*L8/G9HEDCHJ4OIMEDTJCJKDLEDL9CVTAUPIAK29VCN 1UTKFYJZJAPEDI.C\$JC7KDF9CFVAPUB1VCSWC%PDMOLHTC\$JC3EC66CTS89B9F$8H.OOLI7R3Y+95AF3J6FB5R8QMA70Z37244FKG6T\$FJ7CQRB0R%5 47:W0UFJU.UOJ98J93DI+C0UEE-JEJ36VLIWQHH\$QIZB%+N+Y2AW2OP6OH6XO9IE5IVU\$P26J6 L6/E2US2CZU:80I7JM7JHOJKYJPGK:H3J1D1I3-*TW CXBD+$3PY2C725SS+TDM\$SF*SHVT:5D79U+GC5QS+3TAQS:FLU+34IU*9VY-Q9P9SEW-AB+2Q2I56L916CO8T C609O1%NXDU-:R4TICQA.0F2HFLXLLWI8ZU53BMQ2N U:VQQ7RWY91SV2A7N3WQ9J9OAZ00RKLB2"
        val base45Decoder = Base45Decoder()
        val decoder = DefaultCertificateDecoder(base45Decoder)
        val result = decoder.decodeCertificate(hCert)
        assertTrue(result is CertificateDecodingResult.Success)
        val pubkey =
            "MIIBzDCCAXGgAwIBAgIUDN8nWnn8gBmlWgL3stwhoinVD5MwCgYIKoZIzj0EAwIwIDELMAkGA1UEBhMCR1IxETAPBgNVBAMMCGdybmV0LmdyMB4XDTIxMDUxMjExMjY1OFoXDTIzMDUxMjExMjY1OFowIDELMAkGA1UEBhMCR1IxETAPBgNVBAMMCGdybmV0LmdyMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEBcc6ApRZrh9/qCuMnxIRpUujI19bKkG+agj/6rPOiX8VyzfWvhptzV0149AFRWdSoF/NVuQyFcrBoNBqL9zCAqOBiDCBhTAOBgNVHQ8BAf8EBAMCB4AwHQYDVR0OBBYEFN6ZiC57J/yRqTJ/Tg2eRspLCHDhMB8GA1UdIwQYMBaAFNU5HfWNY37TbdZjvsvO+1y1LPJYMDMGA1UdJQQsMCoGDCsGAQQBAI43j2UBAQYMKwYBBAEAjjePZQECBgwrBgEEAQCON49lAQMwCgYIKoZIzj0EAwIDSQAwRgIhAN6rDdE4mtTt2ZuffpZ242/B0lmyvdd+Wy6VuX+J/b01AiEAvME52Y4zqkQDuj2kbfCfs+h3uwYFOepoBP14X+Rd/VM="
        assertTrue(verify(hCert, pubkey))
    }

    @Test
    fun testTaggedJsonCreation() {
        val hCert =
            "HC1:NCFOXNYTSFDHJI8Y0PJ3F1BRV+4$61FI1\$B3LR5XW9G81OGI9JLPZ5M*4MCE AOCV4*XUA2P9FH4%HNTI4L6N\$Q%UG/YL WO*Z7ON1.-LDJ82T9BZJR+HS3WPQO9E4GI8SL0.T70%NUE0J-J:PICIG2R44$2585UN06R2.+K6-CI3J -2DP4TO212JSH0%KNYGFO-O/HLHHJ0GHLHFJW25LO3E8PON QEQ\$HCPPIRH7VHOW2TP9XOA 68ZBG*:J5QNP/IUC7O54TB5PJH+IFO-ON48SU2NUBVYJA.GYS6KRPP+PI+QPRSV-GV*OIUSXQ2SYKLV03RVPCEZY6FNH9.1659Y73JC3DG34LT%F352386B-E3.FJ3LT323UR8K%IU3BSXG NN2*BXEU-IL/UC8AE0VKKWNONG-TL 7928OJCGA7IB9NQ8L59L1PC0D9E2LBHHGKLO-K%FG5IAMEADII-GG\$GKIGQA K%KIO4KPK6PK6F\$BG+SB.V4Q5.AK2EQ6YBK S*%NH\$RSC9KIFX80S\$CKX85JLL%IR299 T757A0L /K1:SY70R61M0VWP0FDA4JBSC9HAG-BJDCI-TL-VC4SLW HPOJZ0K PIS\$S0O29T2*ZE6WUAKEG%5TW5A 6YO67N65VC561CSS2+K/KVMAWCUPK-BT-QYFRZJNU 54*A*K66.AORVFCV.YJ72V34K:1BT1GIFE\$EUXNL606KQHEPB/%9YE96.R L7CRAYIO8MRVBWHLV%.P*YSNDM/0HBZVC*IP:2JIJD:61E77%NT9J3-S726O/5U3F$1CJYV0BSLPS/*D-YN%N4:2FSEQMQUHL5W RXWS-ARH.NMVRUNBYWI8:V29AKXTN2Q:ULZ0SM-F\$JU-0R0J3+TUSVU4FB22N+9BQA8TTT:-6: 6YAN.6W8LBX/CC3BM 24CO6WJM4P8QE0M8XY29B93GQ*AE%/9WH2DI7U2VLW1B3B1:JD%70\$AK%D.R6/NF%LCIWLO.B0DR6OMZHUDOQBP6OU9G3WL/VH:TT07QQU2FOQKLRF5E8VTH7/WR6Y0*:B%75"
        val result = VerificationResult()
        val b45Service: Base45Service = DefaultBase45Service()
        val prefService: PrefixValidationService = DefaultPrefixValidationService()
        val compressorService: CompressorService = DefaultCompressorService()
        val validator: SchemaValidator = DefaultSchemaValidator()
        val coseservice: CoseService = DefaultCoseService()
        val cborservice: CborService = DefaultCborService()
        val base45 = prefService.decode(hCert, result)
        val compressed = b45Service.decode(base45, result)
        val cose = compressorService.decode(compressed, result)
        val cbor = coseservice.decode(cose, result)
        val greenCertificate = cborservice.decodeData(cbor!!.cbor, result)
        try {
            val mapper = ObjectMapper()
            mapper.readTree(greenCertificate!!.hcertJson)
        } catch (e: IOException) {
            Assert.fail()
        }
    }
}