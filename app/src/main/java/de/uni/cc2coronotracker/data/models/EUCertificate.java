package de.uni.cc2coronotracker.data.models;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class EUCertificate implements Serializable {
    public String version;

    // Surnames, human readable
    public String surnames;
    // Surnames as stated on the perso
    public String surnamesStandardised;
    // Forenames (May not exist)
    public String forenames;
    // Standardised forenames
    public String forenamesStandardised;

    // DOB, String, YYYY-MM-DD, YYYY-MM, YYYY only. Missing entries may be X (e.g. 1990-XX-XX)
    public String dateOfBirth;

    public VaccinationGroup vaccinationGroup;
    public TestGroup testGroup;
    public RecoveryGroup recoveryGroup;

    public static class VaccinationGroup implements Serializable {
        public String disease;
        public String prophylaxis;
        public String product;
        public String producer;
        public String dose;
        public String doseRequired;
        public String doseReceived;
        public String doseProvider;
        public String issuer;
        public String identifier;
    }

    public static class TestGroup implements Serializable {
        public String disease;
        public String testProvider;
        public String issuer;
        public String identifier;
        public String testType;
        public String testName;
        public @Nullable
        String testDevice;
        public String sampleDate;
        public String result;
        public String facility;
    }

    public static class RecoveryGroup implements Serializable {
        public String disease;
        public String firstPositiveTest;
        public String testProvider;
        public String issuer;
        public String validFrom;
        public String validUntil;
        public String identifier;
    }
}
