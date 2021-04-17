package dgca.verifier.app.android

import COSE.HeaderKeys
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import dgca.verifier.app.android.chain.CborProcessingChain
import dgca.verifier.app.android.chain.CryptoService
import dgca.verifier.app.android.chain.DefaultBase45Service
import dgca.verifier.app.android.chain.DefaultCborService
import dgca.verifier.app.android.chain.DefaultCompressorService
import dgca.verifier.app.android.chain.DefaultCoseService
import dgca.verifier.app.android.chain.DefaultValSuiteService
import dgca.verifier.app.android.chain.PrefilledCertificateRepository
import dgca.verifier.app.android.chain.RandomEcKeyCryptoService
import dgca.verifier.app.android.chain.VerificationCryptoService
import dgca.verifier.app.android.chain.VerificationResult
import dgca.verifier.app.android.databinding.ActivityMainBinding
import kotlinx.serialization.ExperimentalSerializationApi

private const val CAMERA_REQUEST_CODE = 1003

@ExperimentalUnsignedTypes
@ExperimentalSerializationApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var beepManager: BeepManager
    private var lastText: String? = null

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }
            lastText = result.text
            beepManager.playBeepSoundAndVibrate()

            // Added preview of scanned barcode
            binding.barcodePreview.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW))

            decode(result.text)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val formats: Collection<BarcodeFormat> = listOf(BarcodeFormat.AZTEC, BarcodeFormat.QR_CODE)
        binding.barcodeScanner.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        binding.barcodeScanner.initializeFromIntent(intent)
        binding.barcodeScanner.decodeContinuous(callback)
        beepManager = BeepManager(this)

        binding.resume.setOnClickListener { binding.barcodeScanner.resume() }
        binding.pause.setOnClickListener { binding.barcodeScanner.pause() }

        requestCameraPermission()
    }

    override fun onResume() {
        super.onResume()
        binding.barcodeScanner.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeScanner.pause()
    }

    fun decode(code: String) {
        val cryptoService = RandomEcKeyCryptoService()
        val kid =
            cryptoService.getCborHeaders()
                .first { it.first.AsCBOR() == HeaderKeys.KID.AsCBOR() }.second.AsString()
        val certificate = cryptoService.getCertificate(kid)
        val certificateRepository = PrefilledCertificateRepository()
        certificateRepository.addCertificate(kid, certificate)
        val decodingChain = buildChain(VerificationCryptoService(certificateRepository))
        val verificationResult = VerificationResult()
        val vaccinationData = decodingChain.verify(code, verificationResult)

        binding.barcodeScanner.setStatusText("$code\n  Decoded: \n$vaccinationData")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return binding.barcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    private fun buildChain(cryptoService: CryptoService): CborProcessingChain {
        val coseService = DefaultCoseService(cryptoService)
        val valSuiteService = DefaultValSuiteService()
        val compressorService = DefaultCompressorService()
        val base45Service = DefaultBase45Service()
        val cborService = DefaultCborService()

        return CborProcessingChain(
            cborService,
            coseService,
            valSuiteService,
            compressorService,
            base45Service
        )
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }
    }
}

val trustJson = "[" +
        "{\"kid\" : " +
        "\"DEFBBA3378B322F5\",    " +
        "\"coord\" : [\"230ca0433313f4ef14ec0ab0477b241781d135ee09369507fcf44ca988ed09d6\"," +
        "\"bf1bfe3d2bda606c841242b59c568d00e5c8dd114d223b2f5036d8c5bc68bf5d\"]" +
        "}," +
        "{ \"kid\" : \"FFFBBA3378B322F5\"," +
        "\"coord\" : [\"9999a0433313f4ef14ec0ab0477b241781d135ee09369507fcf44ca988ed09d6\"," +
        "\"9999fe3d2bda606c841242b59c568d00e5c8dd114d223b2f5036d8c5bc68bf5d\"]" +
        "}]"