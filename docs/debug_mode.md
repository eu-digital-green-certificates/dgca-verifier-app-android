<h1 align="center">
    Debug mode
</h1>

## Summary

At times it may be required to capture Digital COVID Certificate (DCC) data in the field for further investigation (e.g. if the DCC  fails verification for no apparent reason; or if (large scale/sophisticated) digital fraud is suspected. Three levels of capture are defined; ranging from one that is fully anonymised (but still allows for verification of the digital seal) to an intermediate one (with just the UVCI, as per the recommendation) and a special level in which a one to one exact copy of the whole QR is made.

## Background

DCCs are rolled out in volume now; by many different countries (and sometimes even by different issuers within a country) each with their own issuer software. This software is generally written from scratch, by independent teams and with a highly diverse set of technologies. Likewise most countries participating have written their own scanners using a similarly diverse set of technologies.  

Software is generally not perfect. And in this case - the standard evolved during the process. 

So with many permutations of issuers, scanners (and near daily software updates), it is likely that we will increasingly need to investigate a ‘RED’ scan in the field, share these scans internationally or turn them into a format suitable for tests.

Citizens of Europe are eager to use the DCC, but sometimes encounter problems on issuance or verification. They call the Helpdesk and want to be helped. They often offer to release the QR  so a solution can be found.

In all these cases (software, publications and help requests) data must be processed in another process than to grant access.

To make the bilateral (or through the eHealth Network) exchanges of this data easier - it is desirable for countries to use similar (good) practices. This makes it easier for all parties to understand what the situation is and to share (debugging) tools.

However a DCC contains private, medical, data. Which can only be stored and exchanged with relatively high safeguard and in exceptional cases (in fact -the Regulation forbids routine capture).  Experience during the first 4 weeks of operation has shown for most (technical) validations and ‘in vivo’ debugging the actual sensitive data is *not needed*. Instead - structure, checksums and digital signatures are more important to preserve.

## Principles

The need to mask personal data - and in particular medical data - has long been a topic of interest in the field of medical informatics. Since with the EU Digital COVID Certificate (DCC) we are dealing with, albeit in a very small measure, medical data then we can turn to established standards for both [pseudonymization and anonymization](https://www.johner-institute.com/articles/software-iec-62304/and-more/anonymization-and-pseudonymization/)

In particular,  the DICOM and HL7 international standards make provision for masking sensitive or personal data ([DICOM de-identification](http://dicom.nema.org/medical/dicom/current/output/html/part15.html#chapter_E), [HL7 anonymization](http://hl7.org/fhir/secpriv-module.html#deId))

In addition, the IHE also provides a description of how to [de-identify data](https://wiki.ihe.net/index.php/Healthcare_De-Identification_Handbook) and there is an ISO standard available (ISO 25237) which deals specifically with how to handle pseudonymization in the context of medical informatics.

Note that there are already free / open-source tools available for both DICOM de-identification (e.g. [gdcm](http://gdcm.sourceforge.net/html/gdcmanon.html), [DICAT](https://github.com/aces/DICAT)) and HL7 anonymization (e.g. [FHIR](https://github.com/microsoft/FHIR-Tools-for-Anonymization)).


## Best Current Practice

Since the EU DCC is neither a complete DICOM Metadata or HL7 data record, then best current practice is to conform to ISO 25237:

"ISO 25237:2017 contains principles and requirements for privacy protection using pseudonymization services for the protection of personal health information. This document is applicable to organizations who wish to undertake pseudonymization processes for themselves or to organizations who make a claim of trustworthiness for operations engaged in pseudonymization services." [5]

For normal capture - all personal data should be masked from the record. This includes all fields in the “nam” field as well as the  UVCI (‘ci’) field. 


EU DCC fields:
- nam
- dob
- [v | t | r] /ci


To aid debugging - the masking should be done such that certain (structural) elements that may be relevant remain (both in the nam, dob and ci fields). 


### General field masking

In the decoded UTF8 sequence; each (unicode) glyph should be replaced according to the following schema for all fields (except the ci field) to a 7bit safe character from the ASCI 32..127 range:


| Unicode 6 category    | Sub-category                 |
| ----------------|-------------------------- |
| Letter (L) group | [LI](https://www.compart.com/en/unicode/category/Ll) (lowercase) by an ‘x’ <br/>[LT](https://www.compart.com/en/unicode/category/LT) (titlecase), [Lu](https://www.compart.com/en/unicode/category/Lu) (Uppercase) by an ‘X’<br/>[Lm](https://www.compart.com/en/unicode/category/Lm) (modifier) by an M <br/>Lo (other) by an R |
| Mark (M) group | [Mc](https://www.compart.com/en/unicode/category/Mc) by an ‘S’, [Me](https://www.compart.com/en/unicode/category/Me), [Mn](https://www.compart.com/en/unicode/category/Mn) by an ‘s’. |
| Number (N) group | [Nd](https://www.compart.com/en/unicode/category/Nd) (digit) in the range U+0030-0039 to an ‘9’, all others to an 8, letter ([Nl](https://www.compart.com/en/unicode/category/Ni)) by a 1,<br/>All others by a 2 |
| Punctuation(P) group | ‘-’ (U+002D) by a ‘-’; ‘.’ (U+002E) by a ‘.’, U+002C) by a ‘,’  remainder of [Pd](https://www.compart.com/en/unicode/category/Pd) (Dash group): ‘=’. Pf/Ps/Pi/Pe (quotes/open) by a ‘Q’ <br/>All others by an ‘!’ |
| Symbol (S) group ([Sc](https://www.compart.com/en/unicode/category/Sc), [Sk](https://www.compart.com/en/unicode/category/Sk), [Sm](https://www.compart.com/en/unicode/category/Sm), [So](https://www.compart.com/en/unicode/category/So)) | By an ‘@’ |
| Separator (Z) and Other (O) Group | Retain space: ‘ ‘(U+0020) by a ‘ ‘;  all others Space ([Zs](https://www.compart.com/en/unicode/category/Zs)) by an ‘_’.  Line ([ZI](https://www.compart.com/en/unicode/category/Zl)), Paragraph ([Zp](https://www.compart.com/en/unicode/category/Zp)) by an N. All others by an ‘?’ |
| Anything else | By the ‘Q’ (U+0071) |


The reason for not mapping all (for example) numbers to a “9” is to distinguish between typical cases that need to be debugged. Such as the common substitution of a lowercase ‘L’(U+006C) for the digit ‘1’ (U+0031). 

For this reason it is critical that no normalisation or any such changes are done to the UTF8 string prior to substitution; as to preserve things such as hidden backspaces, writing order, diacritical marks written as a Combining Character (e.g. U+0300–U+036F), hard spaces, etc.


### DoB field substitution

The date of birth (‘dob’) should be reduced to just the year the remainder of the string should be masked. The reason for preserving the year is to maintain the ability to apply special business logic (e.g. for children or young adults). 

As the 0-9 digits are mapped to a ‘9’ and any alphanumeric character to an ‘X’ it becomes possible to recognise incomplete DoBs (e.g. those lacking the day or month, or using non standard values). This presents a small privacy risk (as this group is relatively small ~ 0.5% of the population).

### UVCI field substitution

For the UVCI field - above defined masking should be applied after the country designator but maintaining the length. This is to aid debugging of extreme/odd lengths (this is unlikely to be an indirect personal data issue - as countries generally issue UVCI’s of very similar and usually identical lengths).  

The masking should be done with the following deviation from above table:


| Unicode 6 category  | Sub-category |
| ----------------|----------------- |
| U+0041..005A | X |
| U+0061..007A | X |
| U+0030..0039 | X |


The reason for this more strict substitution is the relatively high level of entropy in some countries' UVCIs compared to their (much smaller, population sized) combinatorial space. Letting the position of digits/alphanumerics shimmer through would lead to unblinding risks.

## Other residual risks

There is a potential residual risk around the time stamp in the COSE field which is not rounded or mapped out at L1. Future versions may need to mask this field to some number of equal length if actual implementation experience shows that this is an issue in practice.


### Levels disclosed:

| L1 (normal capture) | L2 (traceable capture) | L3 (full take) |
|----------------|-----------------|-----------------|
| n/a | n/a | QR code / photograph |
| n/a | n/a | SHA256 of the decoded QR<br/>Payload of decoded QR as base45*|
| CWT/COSE structure with the payload field replaced by a sequence ‘X’s (i.e. the byte 0x58); same length as the original binary/octet string.| As L1 | SHA256 of the CWT/COSE structureQR<br/>CWT/COSE structure as base64 |
| SHA256 of the actual payload sequence as a HEX sequence (as to still allow sig validation) | As L1 | Base64 representation of the payloadQR<br/>SHA256 of the decoded payload |
| n/a | SHA 256 of the QR | SHA256 of the QR |
|{
  "ver": "1.3.0",
  "nam": {
    "fn": "Xxxxx-Xxxxx",
    "fnt": "XX9XX<XXXX",
    "gn": "Xxxxxxx Xxxxxx",
    "gnt": "XXXXXXX<XXXXXX"
  },
  "dob": "1964-99-99",
  "t": [
    {
      "tg": "840539006",
      "tt": "LP217198-3",
      "ma": "532",
      "sc": "2021-06-11T99:99:99+99",
      "tr": "260415000",
      "co": "NL",
      "is": "Amsterdam PHR",
      "ci": "URN:UVCI:01:NL:XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
    }
  ]
} | 
{
  "ver": "1.3.0",
  "nam": {
    "fn": "Xxxxx-Xxxxx",
    "fnt": "XX9XX<XXXX",
    "gn": "Xxxxxxx Xxxxxx",
    "gnt": "XXXXXXX<XXXXXX"
  },
  "dob": "1964-99-99",
  "t": [
    {
      "tg": "840539006",
      "tt": "LP217198-3",
      "ma": "532",
      "sc": "2021-06-11T17:30:00+02",
      "tr": "260415000",
      "co": "NL",
      "is": "Amsterdam PHR",
      "ci": "URN:UVCI:01:NL:DADFCC47C7334E45A906DB12FD859FB2"
    }
  ]
} | 
{
  "ver": "1.3.0",
  "nam": {
    "fn": "Smith-Jones",
    "fnt": "SM1TH<JONES",
    "gn": "Charles Edward",
    "gnt": "CHARLES<EDWARD"
  },
  "dob": "1964-02-01",
  "t": [
    {
      "tg": "840539006",
      "tt": "LP217198-3",
      "ma": "532",
      "sc": "2021-06-11T17:30:00+02",
      "tr": "260415000",
      "co": "UNHCR",
      "is": "Amsterdam PHR",
      "ci": "URN:UVCI:01:NL:DADFCC47C7334E45A906DB12FD859FB2"
    }
  ]
} |



For L1 and higher - the data handled contains personal data (either just the UVCI in L2, or `everything` at L3). Handling and storage of these requires a set of appropriate organisational and technical measures. As a minimum the principle of four-eyes checking should be in place, with full, independent, auditable logs. In combination with encryption at rest. For L3 it is strongly advised to asymmetrically encrypt the record with controlled decryption key access (e.g. public/private key mechanism).

Note that there is a certain unblinding risk in L2 by revealing the payload SHA256 if the “nam” and “ci” fields are (relatively) short. As the permutation space of a short name and the missing DoB digits is small (10-15 characters with a lot of common names as you know the country, 3-4 digits for the DoB yields).  

For L1 this is less of an issue - as the UCI should be both large and sufficiently securely random.  As this is very unlikely for a real person (and likely the type of anomaly that one is trying to find) this is considered an acceptable, proportional risk.


## International exchange format (version 1.00)

For exchange purposes; it is suggested that member states package the data gathered in a ZIP file ([ISO/IEC 21320-1](https://www.iso.org/standard/60101.html)) file that contains:

- A file called `VERSION.txt` that contains just the 4 byte ASCII string `1.00` (semantic versioning will be used) of this international exchange format followed by a linefeed (LF, 0x0A).
- A `README.txt` that contains some human readable/oriented metadata on the capture process such as the application (version) used, the date, the entity responsible for the capture & contact details, issue/ticket numbers,  and any other information deemed useful such as errors/debug log information or circumstances.
- A 32 byte file `payload-sha.bin` and a 65 byte human readable `payload-sha.txt` that contains the SHA256 of the payload as a case insensitive HEX string terminated by a linefeed.
- A file `QR.base64` that contains the COSE structure (with for L1/2 the payload replaced by an equal number of 0x58 bytes) as a [base64 string](https://datatracker.ietf.org/doc/html/rfc4648).
- A file `payload.json` that contains the decoded JSON (with substitutions depending on the level applied).
- *For  L2 and L3*:
	- A 32 byte file `QR-sha.bin` and a 65 byte, human readable, `QR-sha.txt` file that contains the SHA-256 of the QR as a case insensitive HEX string terminated by a linefeed. 
- *For L3*:
	- A file `QR.png` or `QR.jpg` that contains the scanned QR (*L3 only*)
	- A file `QR.txt` that contains the decoded string from the image as is so prior to HC1 stripping and base45 decoding (*L3 only*))
	- A 32 byte file `cose-sha.bin` and a 65 byte, human readable, `cose-sha.txt` that contains the SHA-256 of the payload as decoded as a case insensitive HEX string terminated by a linefeed. (*L3 only*)
	- A file `cose.base64` that contains the COSE binary (*L3 only*)
	- A file `payload.base64` that contains the payload (*L3 only*)



Dcc settings page:
<img src="/docs/resources/debugMode/settings.png" width="200" />

Debug mode:
<img src="/docs/resources/debugMode/debug.png" width="200" />

Detailed verification view when debug mode is ON:
<img src="/docs/resources/debugMode/debug_certInfo.png" width="200" /> <img src="/docs/resources/debugMode/debug_rawData.png" width="200" /> <img src="/docs/resources/debugMode/debug_content.png" width="200" />

The detailed view displayed when such conditions are met:
- Verification Failed for some reason or has limited validity result;
- Debug mode enabled in settings;
- Issuing country code matches with the selected in settings;
