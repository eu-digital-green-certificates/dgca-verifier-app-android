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
 *  Created by Mykhailo Nester on 4/23/21 9:52 AM
 */

package dgca.verifier.app.decoder.chain

const val JSON_SCHEMA_V1 = "{\n" +
        "    \"\$schema\": \"http://json-schema.org/draft/2020-12/schema#\",\n" +
        "    \"\$id\": \"https://github.com/ehn-digital-green-development/hcert-schema/eu_dgc_v1\",\n" +
        "    \"title\": \"Digital Green Certificate\",\n" +
        "    \"description\": \"Proof of vaccination, test results or recovery according to EU eHN, version 1.0, including certificate metadata; According to 1) REGULATION OF THE EUROPEAN PARLIAMENT AND OF THE COUNCIL on a framework for the issuance, verification and acceptance of interoperable certificates on vaccination, testing and recovery to facilitate free movement during the COVID-19 pandemic (Digital Green Certificate) - https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX%3A52021PC0130 2) Document \\\"Value Sets for the digital green certificate as stated in the Annex ...\\\", abbr. \\\"VS-2021-04-14\\\" - https://webgate.ec.europa.eu/fpfis/wikis/x/05PuKg 3) Guidelines on verifiable vaccination certificates - basic interoperability elements - Release 2 - 2021-03-12, abbr. \\\"guidelines\\\"\",\n" +
        "    \"type\": \"object\",\n" +
        "    \"required\": [\n" +
        "        \"v\",\n" +
        "        \"dgcid\",\n" +
        "        \"sub\"\n" +
        "    ],\n" +
        "    \"properties\": {\n" +
        "        \"v\": {\n" +
        "            \"title\": \"Schema version\",\n" +
        "            \"description\": \"Version of the schema, according to Semantic versioning (ISO, https://semver.org/ version 2.0.0 or newer) (viz. guidelines)\",\n" +
        "            \"type\": \"string\",\n" +
        "            \"example\": \"1.0.0\"\n" +
        "        },\n" +
        "        \"dgcid\": {\n" +
        "            \"title\": \"Identifier\",\n" +
        "            \"description\": \"Unique identifier of the DGC (initially called UVCI (V for vaccination), later renamed to DGCI), format and composizion viz. guidelines\",\n" +
        "            \"type\": \"string\",\n" +
        "            \"example\": \"01AT42196560275230427402470256520250042\"\n" +
        "        },\n" +
        "        \"sub\": {\n" +
        "            \"description\": \"Subject\",\n" +
        "            \"type\": \"object\",\n" +
        "            \"required\": [\n" +
        "                \"gn\",\n" +
        "                \"dob\"\n" +
        "            ],\n" +
        "            \"properties\": {\n" +
        "                \"gn\": {\n" +
        "                    \"title\": \"Given name\",\n" +
        "                    \"description\": \"The given name(s) of the person addressed in the certificate\",\n" +
        "                    \"type\": \"string\",\n" +
        "                    \"example\": \"T\\u00f6lvan\"\n" +
        "                },\n" +
        "                \"fn\": {\n" +
        "                    \"title\": \"Family name\",\n" +
        "                    \"description\": \"The family name(s) of the person addressed in the certificate\",\n" +
        "                    \"type\": \"string\",\n" +
        "                    \"example\": \"T\\u00f6lvansson\"\n" +
        "                },\n" +
        "                \"gnt\": {\n" +
        "                    \"title\": \"Given name (transliterated)\",\n" +
        "                    \"description\": \"The given name(s) of the person addressed in the certificate transliterated into the OCR-B Characters from ISO 1073-2 according to the ICAO Doc 9303 part 3.\",\n" +
        "                    \"type\": \"string\",\n" +
        "                    \"example\": \"Toelvan\"\n" +
        "                },\n" +
        "                \"fnt\": {\n" +
        "                    \"title\": \"Family name (transliterated)\",\n" +
        "                    \"description\": \"The family name(s) of the person addressed in the certificate transliterated into the OCR-B Characters from ISO 1073-2 according to the ICAO Doc 9303 part 3.\",\n" +
        "                    \"type\": \"string\",\n" +
        "                    \"example\": \"Toelvansson\"\n" +
        "                },\n" +
        "                \"id\": {\n" +
        "                    \"title\": \"Person identifiers\",\n" +
        "                    \"description\": \"Identifiers of the vaccinated person, according to the policies applicable in each country\",\n" +
        "                    \"type\": \"array\",\n" +
        "                    \"items\": {\n" +
        "                        \"type\": \"object\",\n" +
        "                        \"required\": [\n" +
        "                            \"t\",\n" +
        "                            \"c\",\n" +
        "                            \"i\"\n" +
        "                        ],\n" +
        "                        \"properties\": {\n" +
        "                            \"t\": {\n" +
        "                                \"title\": \"Identifier type\",\n" +
        "                                \"description\": \"The type of identifier (viz. VS-2021-04-08) PP = Passport Number NN = National Person Identifier (country specified in the 'c' parameter) CZ = Citizenship Card Number HC = Health Card Number\",\n" +
        "                                \"type\": \"string\",\n" +
        "                                \"enum\": [\n" +
        "                                    \"PP\",\n" +
        "                                    \"NN\",\n" +
        "                                    \"CZ\",\n" +
        "                                    \"HC\"\n" +
        "                                ],\n" +
        "                                \"example\": \"NN\"\n" +
        "                            },\n" +
        "                            \"c\": {\n" +
        "                                \"title\": \"Country\",\n" +
        "                                \"description\": \"Issuing country (ISO 3166-1 alpha-2 country code) of identifier\",\n" +
        "                                \"type\": \"string\",\n" +
        "                                \"example\": \"SE\"\n" +
        "                            },\n" +
        "                            \"i\": {\n" +
        "                                \"title\": \"Identifier number or string\",\n" +
        "                                \"type\": \"string\",\n" +
        "                                \"example\": \"121212-1212\"\n" +
        "                            }\n" +
        "                        }\n" +
        "                    }\n" +
        "                },\n" +
        "                \"dob\": {\n" +
        "                    \"title\": \"Date of birth\",\n" +
        "                    \"description\": \"The date of birth of the person addressed in the certificate\",\n" +
        "                    \"type\": \"string\",\n" +
        "                    \"format\": \"date\",\n" +
        "                    \"example\": \"2012-12-12\"\n" +
        "                }\n" +
        "            }\n" +
        "        },\n" +
        "        \"vac\": {\n" +
        "            \"description\": \"Vaccination/prophylaxis information\",\n" +
        "            \"type\": \"array\",\n" +
        "            \"items\": {\n" +
        "                \"type\": \"object\",\n" +
        "                \"required\": [\n" +
        "                    \"dis\",\n" +
        "                    \"vap\",\n" +
        "                    \"mep\",\n" +
        "                    \"aut\",\n" +
        "                    \"seq\",\n" +
        "                    \"tot\",\n" +
        "                    \"dat\",\n" +
        "                    \"cou\"\n" +
        "                ],\n" +
        "                \"properties\": {\n" +
        "                    \"dis\": {\n" +
        "                        \"title\": \"Disease\",\n" +
        "                        \"description\": \"Disease or agent targeted (viz. VS-2021-04-14)\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"840539006\"\n" +
        "                    },\n" +
        "                    \"vap\": {\n" +
        "                        \"title\": \"Vaccine/prophylaxis\",\n" +
        "                        \"description\": \"Generic description of the vaccine/prophylaxis or its component(s), (viz. VS-2021-04-14)\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"1119305005\"\n" +
        "                    },\n" +
        "                    \"mep\": {\n" +
        "                        \"title\": \"Vaccine medicinal product\",\n" +
        "                        \"description\": \"Code of the medicinal product (viz. VS-2021-04-14)\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"EU/1/20/1528\"\n" +
        "                    },\n" +
        "                    \"aut\": {\n" +
        "                        \"title\": \"Vaccine marketing authorization holder or Vaccine manufacturer\",\n" +
        "                        \"description\": \"Code as defined in EMA SPOR - Organisations Management System (viz. VS-2021-04-14)\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"ORG-100030215\"\n" +
        "                    },\n" +
        "                    \"seq\": {\n" +
        "                        \"title\": \"Dose sequence number\",\n" +
        "                        \"description\": \"Number of dose administered in a cycle  (viz. VS-2021-04-14)\",\n" +
        "                        \"type\": \"integer\",\n" +
        "                        \"minimum\": 0,\n" +
        "                        \"example\": 1\n" +
        "                    },\n" +
        "                    \"tot\": {\n" +
        "                        \"title\": \"Total number of doses\",\n" +
        "                        \"description\": \"Number of expected doses for a complete cycle (specific for a person at the time of administration) (viz. VS-2021-04-14)\",\n" +
        "                        \"type\": \"integer\",\n" +
        "                        \"minimum\": 0,\n" +
        "                        \"example\": 2\n" +
        "                    },\n" +
        "                    \"dat\": {\n" +
        "                        \"title\": \"Date of vaccination\",\n" +
        "                        \"description\": \"The date of the vaccination event\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"format\": \"date\",\n" +
        "                        \"example\": \"2021-02-20\"\n" +
        "                    },\n" +
        "                    \"cou\": {\n" +
        "                        \"title\": \"Country\",\n" +
        "                        \"description\": \"Country (member state) of vaccination (ISO 3166-1 alpha-2 Country Code) (viz. VS-2021-04-14)\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"SE\"\n" +
        "                    },\n" +
        "                    \"lot\": {\n" +
        "                        \"title\": \"Batch/lot number\",\n" +
        "                        \"description\": \"A distinctive combination of numbers and/or letters which specifically identifies a batch, optional\",\n" +
        "                        \"type\": \"string\"\n" +
        "                    },\n" +
        "                    \"adm\": {\n" +
        "                        \"title\": \"Administering centre\",\n" +
        "                        \"description\": \"Name/code of administering centre or a health authority responsible for the vaccination event, optional\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"Region Halland\"\n" +
        "                    }\n" +
        "                }\n" +
        "            }\n" +
        "        },\n" +
        "        \"tst\": {\n" +
        "            \"description\": \"Test result statement\",\n" +
        "            \"type\": \"array\",\n" +
        "            \"items\": {\n" +
        "                \"type\": \"object\",\n" +
        "                \"required\": [\n" +
        "                    \"dis\",\n" +
        "                    \"typ\",\n" +
        "                    \"dts\",\n" +
        "                    \"dtr\",\n" +
        "                    \"res\",\n" +
        "                    \"fac\",\n" +
        "                    \"cou\"\n" +
        "                ],\n" +
        "                \"properties\": {\n" +
        "                    \"dis\": {\n" +
        "                        \"title\": \"Disease\",\n" +
        "                        \"description\": \"Disease or agent targeted (viz. VS-2021-04-14)\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"840539006\"\n" +
        "                    },\n" +
        "                    \"typ\": {\n" +
        "                        \"title\": \"Type of test\",\n" +
        "                        \"description\": \"Code of the type of test that was conducted\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"LP6464-4\"\n" +
        "                    },\n" +
        "                    \"tma\": {\n" +
        "                        \"title\": \"Manufacturer and test name\",\n" +
        "                        \"description\": \"Manufacturer and commercial name of the test used (optional for NAAT test) (viz. VS-2021-04-14)\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"tbd\"\n" +
        "                    },\n" +
        "                    \"ori\": {\n" +
        "                        \"title\": \"Sample origin\",\n" +
        "                        \"description\": \"Origin of sample that was taken (e.g. nasopharyngeal swab, oropharyngeal swab etc.) (viz. VS-2021-04-14) optional\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"258500001\"\n" +
        "                    },\n" +
        "                    \"dts\": {\n" +
        "                        \"title\": \"Date and time sample\",\n" +
        "                        \"description\": \"Date and time when the sample for the test was collected (seconds since epoch)\",\n" +
        "                        \"type\": \"integer\",\n" +
        "                        \"minimum\": 0,\n" +
        "                        \"example\": 441759600\n" +
        "                    },\n" +
        "                    \"dtr\": {\n" +
        "                        \"title\": \"Date and time test result\",\n" +
        "                        \"description\": \"Date and time when the test result was produced (seconds since epoch)\",\n" +
        "                        \"type\": \"integer\",\n" +
        "                        \"minimum\": 0,\n" +
        "                        \"example\": 441759600\n" +
        "                    },\n" +
        "                    \"res\": {\n" +
        "                        \"title\": \"Result of test\",\n" +
        "                        \"description\": \"Result of the test according to SNOMED CT (viz. VS-2021-04-14)\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"1240591000000104\"\n" +
        "                    },\n" +
        "                    \"fac\": {\n" +
        "                        \"title\": \"Testing centre or facility\",\n" +
        "                        \"description\": \"Name/code of testing centre, facility or a health authority responsible for the testing event.\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"tbd\"\n" +
        "                    },\n" +
        "                    \"cou\": {\n" +
        "                        \"title\": \"Country\",\n" +
        "                        \"description\": \"Country (member state) of test (ISO 3166-1 alpha-2 Country Code)\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"SE\"\n" +
        "                    }\n" +
        "                }\n" +
        "            }\n" +
        "        },\n" +
        "        \"rec\": {\n" +
        "            \"description\": \"Recovery statement\",\n" +
        "            \"type\": \"array\",\n" +
        "            \"items\": {\n" +
        "                \"type\": \"object\",\n" +
        "                \"required\": [\n" +
        "                    \"dis\",\n" +
        "                    \"dat\",\n" +
        "                    \"cou\"\n" +
        "                ],\n" +
        "                \"properties\": {\n" +
        "                    \"dis\": {\n" +
        "                        \"title\": \"Disease\",\n" +
        "                        \"description\": \"Disease or agent the citizen has recovered from\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"840539006\"\n" +
        "                    },\n" +
        "                    \"dat\": {\n" +
        "                        \"title\": \"Date of first positive test result\",\n" +
        "                        \"description\": \"The date when the sample for the test was collected that led to a positive test result\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"format\": \"date\",\n" +
        "                        \"example\": \"2021-02-20\"\n" +
        "                    },\n" +
        "                    \"cou\": {\n" +
        "                        \"title\": \"Country of test\",\n" +
        "                        \"description\": \"Country (member state) in which the first positive test was performed (ISO 3166-1 alpha-2 Country Code)\",\n" +
        "                        \"type\": \"string\",\n" +
        "                        \"example\": \"SE\"\n" +
        "                    }\n" +
        "                }\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "}"