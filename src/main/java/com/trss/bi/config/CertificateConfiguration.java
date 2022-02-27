package com.trss.bi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class CertificateConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CertificateConfiguration.class);
    final static public String TRUST_STORE_CLASSPATH = "certs/truststore.jks";
    final static public String TRUST_STORE_PASSWORD = "changeit";

    public static void loadCertificates() {
        try {
            ClassPathResource resource = new ClassPathResource(TRUST_STORE_CLASSPATH);
            System.setProperty("javax.net.ssl.trustStore", resource.getURL().toString());
            System.setProperty("javax.net.ssl.trustStorePassword", TRUST_STORE_PASSWORD);
        }
        catch (Exception e) {
            log.error("Unable to load certificates: " + e.getMessage());
        }
    }
}
