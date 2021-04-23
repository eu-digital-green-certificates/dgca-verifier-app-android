package dgca.verifier.app.android

import android.annotation.SuppressLint
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dgca.verifier.app.decoder.chain.base45.Base45Service
import dgca.verifier.app.decoder.chain.cbor.CborService
import dgca.verifier.app.decoder.chain.compression.CompressorService
import dgca.verifier.app.decoder.chain.cose.CoseService
import dgca.verifier.app.decoder.chain.model.GreenCertificate
import dgca.verifier.app.decoder.chain.model.VerificationResult
import dgca.verifier.app.decoder.chain.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.chain.schema.SchemaValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerificationViewModel @ViewModelInject constructor(
    private val prefixValidationService: PrefixValidationService,
    private val base45Service: Base45Service,
    private val compressorService: CompressorService,
    private val coseService: CoseService,
    private val schemaValidator: SchemaValidator,
    private val cborService: CborService
) : ViewModel() {

    private val _verificationResult = MutableLiveData<VerificationResult>()
    val verificationResult: LiveData<VerificationResult> = _verificationResult

    private val _certificate = MutableLiveData<GreenCertificate?>()
    val certificate: LiveData<GreenCertificate?> = _certificate

    fun init(qrCodeText: String) {
        decode(qrCodeText)
    }

    @SuppressLint("SetTextI18n")
    fun decode(code: String) {
        viewModelScope.launch {
            val greenCertificate: GreenCertificate?
            val verificationResult = VerificationResult()

            withContext(Dispatchers.IO) {
                val plainInput = prefixValidationService.decode(code, verificationResult)
                val compressedCose = base45Service.decode(plainInput, verificationResult)
                val cose = compressorService.decode(compressedCose, verificationResult)
                val cbor = coseService.decode(cose, verificationResult)
                schemaValidator.validate(cbor, verificationResult)
                greenCertificate = cborService.decode(cbor, verificationResult)
            }

            _verificationResult.value = verificationResult
            _certificate.value = greenCertificate
        }
    }
}