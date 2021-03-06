package no.difi.certvalidator.rule;

import no.difi.certvalidator.ValidatorBuilder;
import no.difi.certvalidator.api.FailedValidationException;
import no.difi.certvalidator.api.PrincipalNameProvider;
import no.difi.certvalidator.util.SimplePrincipalNameProvider;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.security.cert.CertificateEncodingException;

public class PrincipalNameRuleTest {

    @Test
    public void onlyNoAllowed() throws Exception {
        ValidatorBuilder.newInstance()
                .addRule(new PrincipalNameRule("C", new SimplePrincipalNameProvider("NO")))
                .build()
                .validate(getClass().getResourceAsStream("/peppol-test-ap-difi.cer"));
    }

    @Test(expectedExceptions = FailedValidationException.class)
    public void onlyDkAllowed() throws Exception {
        ValidatorBuilder.newInstance()
                .addRule(new PrincipalNameRule("C", new SimplePrincipalNameProvider("DK")))
                .build()
                .validate(getClass().getResourceAsStream("/peppol-test-ap-difi.cer"));
    }

    @Test(expectedExceptions = FailedValidationException.class)
    public void fullName() throws Exception {
        ValidatorBuilder.newInstance()
                .addRule(new PrincipalNameRule(new PrincipalNameProvider() {
                    @Override
                    public boolean validate(String value) {
                        return value.contains("NORWAY");
                    }
                }, PrincipalNameRule.Principal.SUBJECT))
                .build()
                .validate(getClass().getResourceAsStream("/peppol-test-ap-difi.cer"));
    }

    @Test(expectedExceptions = FailedValidationException.class)
    public void triggerCertificateEncodingException() throws Exception {
        PrincipalNameProvider provider = Mockito.mock(PrincipalNameProvider.class);
        Mockito.doThrow(CertificateEncodingException.class).when(provider).validate(Mockito.anyString());

        ValidatorBuilder.newInstance()
                .addRule(new PrincipalNameRule(provider))
                .build()
                .validate(getClass().getResourceAsStream("/peppol-test-ap-difi.cer"));
    }
}
