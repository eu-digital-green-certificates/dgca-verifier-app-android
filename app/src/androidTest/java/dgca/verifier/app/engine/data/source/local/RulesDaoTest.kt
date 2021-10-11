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
 *  Created by osarapulov on 8/2/21 9:21 AM
 */

package dgca.verifier.app.engine.data.source.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.databind.ObjectMapper
import dgca.verifier.app.android.data.local.rules.EngineDatabase
import dgca.verifier.app.android.data.local.model.RuleWithDescriptionsLocal
import dgca.verifier.app.android.data.local.rules.RulesDao
import dgca.verifier.app.android.data.local.rules.toRuleWithDescriptionLocal
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.source.remote.rules.RuleRemote
import dgca.verifier.app.engine.data.source.remote.rules.toRule
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
internal class RulesDaoTest {
    private lateinit var rulesDao: RulesDao
    private lateinit var db: EngineDatabase
    private val objectMapper = ObjectMapper().apply { this.findAndRegisterModules() }

    companion object {
        const val RULE_JSON_FILE_NAME = "rule.json"
        const val RULE_WITH_REGION_JSON_FILE_NAME = "rule_with_region.json"
    }

    private fun fetchRule(fileName: String): RuleRemote {
        val ruleExampleIs: InputStream =
            javaClass.classLoader!!.getResourceAsStream(fileName)
        val ruleJson = IOUtils.toString(ruleExampleIs, Charset.defaultCharset())
        return objectMapper.readValue(ruleJson, RuleRemote::class.java)
    }


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, EngineDatabase::class.java
        ).build()
        rulesDao = db.rulesDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testInsert() {
        val rule = fetchRule(RULE_JSON_FILE_NAME).toRule()
        val expected: RuleWithDescriptionsLocal = rule.toRuleWithDescriptionLocal()
        rulesDao.insertAll(listOf(expected))

        assertTrue(
            rulesDao.getRulesWithDescriptionsBy(
                rule.countryCode,
                rule.validTo.plusDays(1),
                rule.type,
                rule.ruleCertificateType,
                RuleCertificateType.GENERAL
            ).isEmpty()
        )

        val actual = rulesDao.getRulesWithDescriptionsBy(
            rule.countryCode,
            rule.validTo.minusMinutes(1),
            rule.type,
            rule.ruleCertificateType,
            RuleCertificateType.GENERAL
        )

        assertTrue(actual.size == 1)
        assertEquals(expected.rule.copy(ruleId = 1), actual[0].rule)
        assertEquals(2, actual[0].descriptions.size)
        expected.descriptions.forEachIndexed { index, descriptionLocal ->
            assertEquals(
                descriptionLocal.copy(
                    descriptionId = (index + 1).toLong(),
                    ruleContainerId = 1
                ), actual[0].descriptions[index]
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun testInsertRuleWithRegion() {
        val ruleRemote = fetchRule(RULE_WITH_REGION_JSON_FILE_NAME).toRule()
        val expected: RuleWithDescriptionsLocal = ruleRemote.toRuleWithDescriptionLocal()
        rulesDao.insertAll(listOf(expected))

        assertTrue(
            rulesDao.getRulesWithDescriptionsBy(
                ruleRemote.countryCode,
                ruleRemote.validTo.plusDays(1),
                ruleRemote.type,
                ruleRemote.ruleCertificateType,
                RuleCertificateType.GENERAL
            ).isEmpty()
        )

        val actual = rulesDao.getRulesWithDescriptionsBy(
            ruleRemote.countryCode,
            ruleRemote.validTo.minusMinutes(1),
            ruleRemote.type,
            ruleRemote.ruleCertificateType,
            RuleCertificateType.GENERAL
        )

        assertTrue(actual.size == 1)
        assertEquals(expected.rule.copy(ruleId = 1), actual[0].rule)
        expected.descriptions.forEachIndexed { index, descriptionLocal ->
            assertEquals(
                descriptionLocal.copy(
                    descriptionId = (index + 1).toLong(),
                    ruleContainerId = 1
                ), actual[0].descriptions[index]
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDelete() {
        val ruleRemote = fetchRule(RULE_JSON_FILE_NAME).toRule()
        val expected: RuleWithDescriptionsLocal = ruleRemote.toRuleWithDescriptionLocal()
        rulesDao.insertAll(listOf(expected))

        assertEquals(1, rulesDao.getAll().size)
        assertEquals(2, rulesDao.getDescriptionAll().size)

        rulesDao.deleteRulesBy(listOf(ruleRemote.identifier))

        assertEquals(0, rulesDao.getAll().size)
        assertEquals(0, rulesDao.getDescriptionAll().size)
    }

    @Test
    @Throws(Exception::class)
    fun testDeleteAllExcept() {
        val identifierFirst = "identifierFirst"
        val identifierSecond = "identifierSecond"
        val ruleFirst = fetchRule(RULE_JSON_FILE_NAME).toRule().copy(identifier = identifierFirst)
        val ruleSecond = fetchRule(RULE_JSON_FILE_NAME).toRule().copy(identifier = identifierSecond)
        val ruleWithDescriptionsLocalFirst: RuleWithDescriptionsLocal =
            ruleFirst.toRuleWithDescriptionLocal()
        val ruleWithDescriptionsLocalSecond: RuleWithDescriptionsLocal =
            ruleSecond.toRuleWithDescriptionLocal()

        rulesDao.insertAll(listOf(ruleWithDescriptionsLocalFirst, ruleWithDescriptionsLocalSecond))

        assertEquals(2, rulesDao.getAll().size)

        rulesDao.deleteAllExcept(arrayOf(identifierSecond))

        val rulesLocalActual = rulesDao.getAll()
        assertEquals(1, rulesLocalActual.size)
        assertEquals(identifierSecond, rulesLocalActual.first().identifier)
    }
}