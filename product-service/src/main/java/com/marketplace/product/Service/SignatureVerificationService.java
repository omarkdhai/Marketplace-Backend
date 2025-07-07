package com.marketplace.product.Service;

import jakarta.enterprise.context.ApplicationScoped;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@ApplicationScoped
public class SignatureVerificationService {

    private static final String PERSONAL_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

    public boolean verifySignature(String signature, String signerAddress, String message) {
        if (signature == null || signerAddress == null || message == null) {
            System.err.println("Signature, address, or message is null. Verification failed.");
            return false;
        }

        // Recreate the message hash that was signed on the client
        String prefixedMessage = PERSONAL_MESSAGE_PREFIX + message.length() + message;
        byte[] messageHash = org.web3j.crypto.Hash.sha3(prefixedMessage.getBytes(StandardCharsets.UTF_8));

        byte[] signatureBytes = Numeric.hexStringToByteArray(signature);

        // The 'v' value is the last byte of the signature
        byte v = signatureBytes[64];
        if (v < 27) {
            v += 27;
        }

        byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
        byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);
        Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);

        try {
            // Recover the public key from the signature and the message hash
            BigInteger recoveredPublicKey = Sign.signedMessageHashToKey(messageHash, signatureData);
            String recoveredAddress = "0x" + Keys.getAddress(recoveredPublicKey);

            // Compare the recovered address to the claimed signer's address
            boolean isValid = signerAddress.equalsIgnoreCase(recoveredAddress);
            if (!isValid) {
                System.err.printf("Signature verification failed. Expected: %s, Recovered: %s%n", signerAddress, recoveredAddress);
            }
            return isValid;

        } catch (Exception e) {
            System.err.println("Exception during signature verification: " + e.getMessage());
            return false;
        }
    }
}
