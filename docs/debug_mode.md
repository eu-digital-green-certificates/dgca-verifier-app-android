<h1 align="center">
    Debug mode
</h1>

### Summary

At times it may be required to capture Digital COVID Certificate (DCC) data in the field for further investigation (e.g. if the DCC  fails verification for no apparent reason; or if (large scale/sophisticated) digital fraud is suspected. Three levels of capture are defined; ranging from one that is fully anonymised (but still allows for verification of the digital seal) to an intermediate one (with just the UVCI, as per the recommendation) and a special level in which a one to one exact copy of the whole QR is made.

### Background

DCCs are rolled out in volume now; by many different countries (and sometimes even by different issuers within a country) each with their own issuer software. This software is generally written from scratch, by independent teams and with a highly diverse set of technologies. Likewise most countries participating have written their own scanners using a similarly diverse set of technologies.  

Software is generally not perfect. And in this case - the standard evolved during the process. 

So with many permutations of issuers, scanners (and near daily software updates), it is likely that we will increasingly need to investigate a ‘RED’ scan in the field, share these scans internationally or turn them into a format suitable for tests.

Citizens of Europe are eager to use the DCC, but sometimes encounter problems on issuance or verification. They call the Helpdesk and want to be helped. They often offer to release the QR  so a solution can be found.

In all these cases (software, publications and help requests) data must be processed in another process than to grant access.

To make the bilateral (or through the eHealth Network) exchanges of this data easier - it is desirable for countries to use similar (good) practices. This makes it easier for all parties to understand what the situation is and to share (debugging) tools.

However a DCC contains private, medical, data. Which can only be stored and exchanged with relatively high safeguard and in exceptional cases (in fact -the Regulation forbids routine capture).  Experience during the first 4 weeks of operation has shown for most (technical) validations and ‘in vivo’ debugging the actual sensitive data is *not needed*. Instead - structure, checksums and digital signatures are more important to preserve.

### Principles

The need to mask personal data - and in particular medical data - has long been a topic of interest in the field of medical informatics. Since with the EU Digital COVID Certificate (DCC) we are dealing with, albeit in a very small measure, medical data then we can turn to established standards for both [pseudonymization and anonymization](https://www.johner-institute.com/articles/software-iec-62304/and-more/anonymization-and-pseudonymization/)

In particular,  the DICOM and HL7 international standards make provision for masking sensitive or personal data ([DICOM de-identification](http://dicom.nema.org/medical/dicom/current/output/html/part15.html#chapter_E), [HL7 anonymization](http://hl7.org/fhir/secpriv-module.html#deId))

In addition, the IHE also provides a description of how to [de-identify data](https://wiki.ihe.net/index.php/Healthcare_De-Identification_Handbook) and there is an ISO standard available (ISO 25237) which deals specifically with how to handle pseudonymization in the context of medical informatics.

Note that there are already free / open-source tools available for both DICOM de-identification (e.g. [gdcm](http://gdcm.sourceforge.net/html/gdcmanon.html), [DICAT](https://github.com/aces/DICAT)) and HL7 anonymization (e.g. [FHIR](https://github.com/microsoft/FHIR-Tools-for-Anonymization)).


### Best Current Practice

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





TODO: update images

<img src="/docs/resources/debugMode/settings.png" width="200" />
<img src="/docs/resources/debugMode/debug.png" width="200" />
<img src="/docs/resources/debugMode/debug_certInfo.png" width="200" />
<img src="/docs/resources/debugMode/debug_rawData.png" width="200" />
<img src="/docs/resources/debugMode/debug_content.png" width="200" />
