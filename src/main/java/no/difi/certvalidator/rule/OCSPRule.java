package no.difi.certvalidator.rule;

import no.difi.certvalidator.api.CertificateBucket;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.api.FailedValidationException;
import no.difi.certvalidator.api.ValidatorRule;
import org.bouncycastle.asn1.x509.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.provider.certpath.OCSP;

import java.io.IOException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.X509Certificate;

/**
 * Validation of certificate using OCSP. Requires intermediate certificates.
 */
public class OCSPRule implements ValidatorRule {

    private static final Logger logger = LoggerFactory.getLogger(OCSPRule.class);

    private CertificateBucket intermediateCertificates;

    public OCSPRule(CertificateBucket intermediateCertificates) {
        this.intermediateCertificates = intermediateCertificates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(X509Certificate certificate) throws CertificateValidationException {
        try {
            // Certificates without OCSP information is not subject to OCSP validation.
            if (certificate.getExtensionValue(Extension.authorityInfoAccess.getId()) == null)
                return;

            X509Certificate issuer = intermediateCertificates.findBySubject(certificate.getIssuerX500Principal());
            if (issuer == null)
                throw new FailedValidationException(String.format("Unable to find issuer certificate '%s'", certificate.getIssuerX500Principal().getName()));

            OCSP.RevocationStatus status = getRevocationStatus(certificate, issuer);

            if (!status.getCertStatus().equals(OCSP.RevocationStatus.CertStatus.GOOD))
                throw new FailedValidationException("Certificate status is not reported as GOOD by OCSP.");
        } catch (CertificateValidationException e) {
            logger.debug("{} ({})", e.getMessage(), certificate.getSerialNumber());
            throw e;
        } catch (Exception e) {
            logger.debug("{} ({})", e.getMessage(), certificate.getSerialNumber());
            throw new CertificateValidationException(e.getMessage(), e);
        }
    }

    public OCSP.RevocationStatus getRevocationStatus(X509Certificate cert, X509Certificate issuer) throws IOException, CertPathValidatorException {
        return OCSP.check(cert, issuer);
    }
}
