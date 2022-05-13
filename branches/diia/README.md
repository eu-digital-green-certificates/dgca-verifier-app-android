<h1 align="center">
    EU Digital COVID Certificate Verifier App - Android
</h1>

<p align="center">
    <a href="/../../commits/" title="Last Commit"><img src="https://img.shields.io/github/last-commit/eu-digital-green-certificates/dgca-verifier-app-android?style=flat"></a>
    <a href="/../../issues" title="Open Issues"><img src="https://img.shields.io/github/issues/eu-digital-green-certificates/dgca-verifier-app-android?style=flat"></a>
    <a href="./LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

<p align="center">
  <a href="#about">About</a> •
  <a href="#documentation">Documentation</a> •
</p>

## About

The module represents Ukrainian service Diia certificates.

## Documentation  

Below is the documentation for certificates JSON structure.

Note: certificates has similar structure with DCC certificates - that's why so far Diia and Dcc module couldn't be used together at once.
It's required to inject either one module (dcc) or another (diia)

```
root->dob - Date of birth

root->nam - Surname(s), given name(s) - in that order
root->nam->fn - Family name
root->nam->gn - Given name
root->nam->fnt - Standardised family name
root->nam->gnt - Standardised given name

root->rnokpp - Some certificate identifier
root->docType - Document type in Ukrainian. Example: Ukrainian passport (Паспорт громадянина України)
root->birthday - Date of birth
root->genderEN - Gender in English. Example: Male (M), Female (F)
root->genderUA - Gender in Ukrainian. Example: Male (Ч - чоловік), Female (Ж - жінка)
root->docNumber - Document number
root->lastNameEN - Family name in English
root->lastNameUA - Family name in Ukrainian
root->firstNameEN - Given name in English
root->firstNameUA - Given name in Ukrainian
root->middleNameUA - Father name in Ukrainian
root->registration - Registration place in Ukrainian
root->nationalityEN - Nationality in English
root->nationalityUA - Nationality in Ukrainian

root->ver - Schema version
```

```
{
  1: {
    "v": [

    ],
    "dob": "03.03.2003",
    "nam": {
      "fn": "Петраков",
      "gn": "Олександр",
      "fnt": "Petrakov",
      "gnt": "Oleksandr"
    },
    "uId": {
      "rnokpp": "6354735273",
      "docType": "Паспорт громадянина України",
      "birthday": "03.03.2003",
      "genderEN": "F",
      "genderUA": "Ч",
      "docNumber": "94857437-88363",
      "lastNameEN": "Petrakov",
      "lastNameUA": "Петраков",
      "firstNameEN": "Oleksandr",
      "firstNameUA": "Олександр",
      "middleNameUA": "Васильович",
      "registration": "UA, м.Київ, майдан Незалежності 1",
      "nationalityEN": "UA",
      "nationalityUA": "UA"
    },
    "ver": "1.3.0"
  }
}
```
