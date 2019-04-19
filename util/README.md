# WaterAuth SAML AuthNResponse Debug Tool
## Compatability
I developed these on an Ubuntu VM and have not tested them elsewhere. I presume they'll work anywhere that has access to `openssl`, `base64`, and `dd` but I can't confirm that with certainty.

## What is this?
### TL;DR
This small command-line utility can be used to view the raw contents of a SAML AuthNResponse being fed into Water Auth. This is useful if you need to see what AD groups your user is part of or if you want to examine any other components of the AuthNResponse for debugging purposes.

### Detailed
When making a request to DOI SAML Water Auth makes a SAML AuthNRequest and forwards it via a browser redirect of the user. This puts the user onto the DOI AD login page, and once the user has logged in they are redirected back to Water Auth with the DOI AD SAML AuthNResponse as a request parameter.

Part of this process involves encrypting sensitive data within the SAML AuthNResponse so that it cannot be read by anything other than Water Auth. This is accomplished by Water Auth providing the public key half of its SAML Keypair in the AuthNRequest. When DOI AD is creating the SAML AuthNResponse rather than encrypting the entire thing using RSA Public/Private Key encryption (which is slower), it generates a random 16-bit AES key and encrypts the sensitive portion of the response with that (which is faster). Then, using the public half of Water Auth's SAML key pair, it encrypts the 16-bit AES key using RSA Public/Private Key encryption, meaning the slow encryption was only used on the 16-bit AES key and not the entire sensitive response, which is likely much larger than 16 bits.

When the response returns to Water Auth it must first use its SAML private key to decrypt the AES key, and then use the decrypted AES key to decrypt the sensitive portion of the response.

This utility can be used to emulate the second half of this process locally using the Water Auth SAML private key.

## How do I use it?
### TL;DR
1. `decode_saml.sh path_to_saml_response_from_devtools_file path_of_result_file`

2. `decrypt_saml.sh path_to_encrypted_aes_cipher_key_file path_to_encrypted_assertion_file path_to_water_auth_saml_private_key_file path_of_result_file`

### Detailed
1. In a new browser tab open the browser DevTools, activate the Network tab, and ensure network requests are being logged.

2. Navigate to WaterAuth (auth.nwis.usgs.gov). If you haven't visited Water Auth recently you should be redirected through the DOI AD login flow. If you aren't try this in an incognito tab or another browser.

3. Examine the network requests in the DevTools window for an entry performing a POST to `https://auth.nwis.usgs.gov/saml/SSO`. If you examine the parameters sent with that request you should see an entry called `SAMLResponse` with a big block of text. This is the base64-encoded SAML AuthNResponse sent from DOI AD to Water Auth.

4. Copy the SAMLResponse value, not including the parameter name I.E: Everything after '`SAMLResponse=`' and save it in a file. 

5. Currently the SAMLResponse is URL and Base64 encoded so we need to decode it back to XML. This can be done using the included `decode_saml.sh` utility. This utility expects two arguments:
    1. The path to the file in which you saved the `SAMLResponse` value.
    2. The name/path for where you'd like to save the result.
    
    Example:
        `decode_saml.sh ./saml_response.txt ./decoded_saml.xml`

6. Now that we have the decoded SAML XML we need to extract two bits of information from it:
    1. The EncryptedAssertion KeyInfo CipherValue (The encrypted AES key used to encrypt the Assertion value)
    2. The EncryptedAssertion CipherValue (The AES-encrypted sensitive section of the XML).

7. Open the decoded SAML XML into a text editor, preferably one that supports pretty-printing XML (although you could also use another tool to do this safely at this point because the sensitive data is still encrypted).

8. Copy the value of `EncryptedAssertion --> EncryptedData --> KeyInfo --> EncryptedKey --> CipherData --> CipherValue` into a file.

9. Copy the value of `EncryptedAssertion --> EncryptedData --> CipherData --> CipherValue` into a file.

10. Retrieve a copy of the Water Auth SAML Private Key file. This is stored in S3. Ask another developer for the location of the key in S3 if you do not know.

11. You can now run the `decrypt_saml.sh` script using the files created in steps 8, 9, and 10 to decrypt the `EncryptedAssertion` value and view the contents. 

    Example exectuion (`saml.key`: Step 10, `enc_data.txt`: Step 9, `enc_key.txt`: Step 8): 

    `./decrypt_saml.sh enc_key.txt enc_data.txt saml.key decrypted_assertion.xml`