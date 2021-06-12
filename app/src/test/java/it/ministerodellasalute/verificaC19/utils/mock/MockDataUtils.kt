package it.ministerodellasalute.verificaC19.utils.mock

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

class MockDataUtils {

    companion object {

        val ASSET_BASE_PATH = "src/test/assets/"
        val GSON = Gson()

        @JvmStatic
        @Throws(IOException::class)
        fun readFile(filename: String): String {
            val br = BufferedReader(InputStreamReader(FileInputStream(ASSET_BASE_PATH + filename)))
            val sb = StringBuilder()
            var line: String? = br.readLine()
            while (line != null) {
                sb.append(line)
                line = br.readLine()
            }

            return sb.toString()
        }
    }
}