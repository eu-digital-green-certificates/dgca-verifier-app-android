package dgca.verifier.app.android.chain

class SampleData {

    companion object {
        val recovery = """
        {
            "sub": {
                "gn": "Gabriele",
                "fn": "Musterfrau",
                "id": [
                    {
                        "t": "PPN",
                        "i": "12345ABC-321"
                    }
                ],
                "dob": "1998-02-26",
                "gen": "female"
            },
            "rec": [
                {
                    "dis": "840539006",
                    "dat": "2021-02-20",
                    "cou": "AT"
                }
            ],
            "cert": {
                "is": "Ministry of Health, Austria",
                "id": "01AT42196560275230427402470256520250042",
                "vf": "2021-04-04",
                "vu": "2021-10-04",
                "co": "AT",
                "vr": "v1.0"
            }
        }
        """.trimIndent()
        val vaccination = """
        {
            "sub": {
                "gn": "Gabriele",
                "fn": "Musterfrau",
                "id": [
                    {
                        "t": "PPN",
                        "i": "12345ABC-321"
                    }
                ],
                "dob": "1998-02-26",
                "gen": "female"
            },
            "vac": [
                {
                    "dis": "840539006",
                    "vap": "1119305005",
                    "mep": "EU\/1\/20\/1528",
                    "aut": "ORG-100030215",
                    "seq": 1,
                    "tot": 2,
                    "dat": "2021-02-18",
                    "cou": "AT",
                    "lot": "C22-862FF-001",
                    "adm": "Vaccination centre Vienna 23"
                },
                {
                    "dis": "840539006",
                    "vap": "1119305005",
                    "mep": "EU\/1\/20\/1528",
                    "aut": "ORG-100030215",
                    "seq": 2,
                    "tot": 2,
                    "dat": "2021-03-12",
                    "cou": "AT",
                    "lot": "C22-H62FF-010",
                    "adm": "Vaccination centre Vienna 23"
                }
            ],
            "cert": {
                "is": "Ministry of Health, Austria",
                "id": "01AT42196560275230427402470256520250042",
                "vf": "2021-04-04",
                "vu": "2021-10-04",
                "co": "AT",
                "vr": "v1.0"
            }
        }
        """.trimIndent()
        val test = """
        {
            "sub": {
                "gn": "Gabriele",
                "fn": "Musterfrau",
                "id": [
                    {
                        "t": "PPN",
                        "i": "12345ABC-321"
                    }
                ],
                "dob": "1998-02-26",
                "gen": "female"
            },
            "tst": [
                {
                    "dis": "840539006",
                    "typ": "LP6464-4",
                    "tna": "tbd tbd tbd",
                    "tma": "tbd tbd tbd",
                    "ori": "258500001",
                    "dts": "2021-02-20T12:34:56+00:00",
                    "dtr": "2021-02-20T14:56:01+00:00",
                    "res": "1240591000000102",
                    "fac": "Testing center Vienna 1",
                    "cou": "AT"
                }
            ],
            "cert": {
                "is": "Ministry of Health, Austria",
                "id": "01AT42196560275230427402470256520250042",
                "vf": "2021-04-04",
                "vu": "2021-10-04",
                "co": "AT",
                "vr": "v1.0"
            }
        }
        """.trimIndent()
    }

}
