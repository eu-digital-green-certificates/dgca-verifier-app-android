package dgca.verifier.app.decoder.chain

import android.util.Log
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.upokecenter.cbor.CBORObject
import dgca.verifier.app.decoder.chain.cwt.CwtHeaderKeys
import dgca.verifier.app.decoder.chain.model.VerificationResult

class SchemaValidator {

    fun validate(cbor: ByteArray, verificationResult: VerificationResult): Boolean {
        val map = CBORObject.DecodeFromBytes(cbor)
        val hcert = map[CwtHeaderKeys.HCERT.AsCBOR()]
        val hcertv1 = hcert[CBORObject.FromObject(1)].GetByteString()

        val json = CBORObject.DecodeFromBytes(hcertv1).ToJSONString()
        val mapper = ObjectMapper()
        val schemaNode: JsonNode = mapper.readTree(JSON_SCHEMA_V1)
        val jsonNode: JsonNode = mapper.readTree(json)

        val factory = JsonSchemaFactory.byDefault()
        val schema: JsonSchema = factory.getJsonSchema(schemaNode)

        val report: ProcessingReport = schema.validate(jsonNode)
        Log.d("schema validation", "report: $report")

        val isValid = report.isSuccess
        verificationResult.isSchemaValid = isValid

        return isValid
    }
}