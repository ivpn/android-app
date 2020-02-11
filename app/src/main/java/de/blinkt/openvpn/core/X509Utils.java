
package de.blinkt.openvpn.core;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Vector;

import de.blinkt.openvpn.VpnProfile;

public class X509Utils {

    public static Certificate[] getCertificatesFromFile(String certificateFileName)
            throws FileNotFoundException, CertificateException {
        CertificateFactory certFact = CertificateFactory.getInstance("X.509");

        Vector<Certificate> certificates = new Vector<>();
        if (VpnProfile.isEmbedded(certificateFileName)) {
            int subIndex = certificateFileName.indexOf("-----BEGIN CERTIFICATE-----");
            do {
                // The java certifcate reader is ... kind of stupid
                // It does NOT ignore chars before the --BEGIN ...

                subIndex = Math.max(0, subIndex);
                InputStream inStream = new ByteArrayInputStream(certificateFileName.substring(subIndex).getBytes());
                certificates.add(certFact.generateCertificate(inStream));

                subIndex = certificateFileName.indexOf("-----BEGIN CERTIFICATE-----", subIndex + 1);
            } while (subIndex > 0);
            return certificates.toArray(new Certificate[certificates.size()]);
        } else {
            InputStream inStream = new FileInputStream(certificateFileName);
            return new Certificate[]{certFact.generateCertificate(inStream)};
        }
    }
}