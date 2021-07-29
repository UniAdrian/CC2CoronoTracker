package de.uni.cc2coronotracker.helper;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.common.util.Hex;
import com.google.iot.cbor.CborArray;
import com.google.iot.cbor.CborConversionException;
import com.google.iot.cbor.CborMap;
import com.google.iot.cbor.CborObject;
import com.google.iot.cbor.CborParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import COSE.CoseException;
import COSE.Message;
import COSE.MessageTag;
import COSE.Sign1Message;
import de.uni.cc2coronotracker.data.qr.QrIntent;
import nl.minvws.encoding.Base45;

public class EGCHelper {

    private static final String TAG = "EGCHelper";


    public static QrIntent.EGC parse(String toParse) throws CborConversionException, CborParseException, IOException, DataFormatException, CoseException {

        byte[] decodedBytes = Base45.getDecoder().decode(toParse.substring(4));
        byte[] decompressed = decompress(decodedBytes);

        Sign1Message msg = (Sign1Message) Message.DecodeFromBytes(decompressed, MessageTag.Sign1);
        byte[] content = msg.GetContent();

        Log.d(TAG, Hex.bytesToStringLowercase(decompressed));
        Log.d(TAG, Hex.bytesToStringLowercase(content));

        // At this point we should have a valid cbor web token. Y u no use JWT?
        CborMap cwt = CborMap.createFromCborByteArray(content);

        CborObject issuerKey = CborObject.createFromJavaObject(1);
        CborObject expirationKey = CborObject.createFromJavaObject(4);
        CborObject issuedAtKey = CborObject.createFromJavaObject(6);
        CborObject healthCertKey = CborObject.createFromJavaObject(-260);

        CborObject issuer = cwt.get(issuerKey);
        CborObject expirationTime = cwt.get(expirationKey);
        CborObject issuedAt = cwt.get(issuedAtKey);

        CborObject healthCert = cwt.get(healthCertKey);

        List<EUCertificate> parsedCerts = new ArrayList<>();
        CborMap certificates = CborMap.createFromCborByteArray(healthCert.toCborByteArray());
        for (Map.Entry<CborObject, CborObject> setEntry : certificates.entrySet()) {
            try {
                CborMap cert = CborMap.createFromCborByteArray(setEntry.getValue().toCborByteArray());

                EUCertificate euCert = new EUCertificate();

                euCert.version = cert.get("ver").toString();

                CborMap names = CborMap.createFromCborByteArray(cert.get("nam").toCborByteArray());
                euCert.surnames = names.get("fn").toString();
                euCert.surnamesStandardised = names.get("fnt").toString();

                if (names.containsKey("gn"))
                    euCert.forenames = names.get("gn").toString();
                if (names.containsKey("gnt"))
                    euCert.forenamesStandardised = names.get("gnt").toString();

                euCert.dateOfBirth = cert.get("dob").toString();


                /**
                 * Each certificate must have exactly one group:
                 * v: vaccination group
                 * t: test group
                 * r: recovery group
                 */
                if (cert.containsKey("v")) {
                    euCert.vaccinationGroup = parseVaccinationGroup(cert.get("v"));
                } else if (cert.containsKey("t")) {
                    euCert.testGroup = parseTestGroup(cert.get("t"));
                } else if (cert.containsKey("r")) {
                    euCert.recoveryGroup = parseRecoveryGroup(cert.get("r"));
                } else {
                    throw new IllegalArgumentException("Invalid EGC: Must contain exactly one group: v, t or r");
                }

                parsedCerts.add(euCert);
            } catch (Exception e) {
                Log.e(TAG, "Not a valid EU cert", e);
            }
        }

        if (parsedCerts.isEmpty()) {
            throw new IllegalArgumentException("Invalid EGC: Must contain at least one valid health certificate.");
        }

        QrIntent.EGC egc = new QrIntent.EGC();
        egc.raw = toParse;
        egc.issuer = issuer.toString();
        egc.expiration = Long.valueOf(expirationTime.toString());
        egc.issuedAt = Long.valueOf(issuedAt.toString());

        egc.certificates = parsedCerts;

        return egc;
    }

    private static VaccinationGroup parseVaccinationGroup(CborObject v) throws CborParseException {
        // For some reason they use an array even tho the spec requires exactly one entry ... always...
        Log.d(TAG, "Parsing VaccineGroup: " + v.toString());
        CborArray arr = (CborArray)v;

        CborMap group = CborMap.createFromCborByteArray(arr.listValue().get(0).toCborByteArray());
        VaccinationGroup vg = new VaccinationGroup();

        vg.disease = group.get("tg").toString();
        vg.prophylaxis = group.get("vp").toString();

        vg.product = group.get("mp").toString();
        vg.producer = group.get("ma").toString();

        vg.dose = group.get("dn").toString();
        vg.doseRequired = group.get("sd").toString();
        vg.doseReceived = group.get("dt").toString();
        vg.doseProvider = group.get("co").toString();

        vg.issuer = group.get("is").toString();
        vg.identifier = group.get("ci").toString();

        return vg;
    }

    private static TestGroup parseTestGroup(CborObject t) throws CborParseException {
        // For some reason they use an array even tho the spec requires exactly one entry ... always...
        Log.d(TAG, "Parsing TestGroup: " + t.toString());
        CborArray arr = (CborArray)t;

        CborMap group = CborMap.createFromCborByteArray(arr.listValue().get(0).toCborByteArray());
        TestGroup tg = new TestGroup();

        tg.disease = group.get("tg").toString();

        tg.testType = group.get("tt").toString();

        if (group.containsKey("nm"))
            tg.testName = group.get("nm").toString();

        if (group.containsKey("ma"))
            tg.testDevice = group.get("ma").toString();

        tg.sampleDate = group.get("sc").toString();
        tg.resultDate = group.get("tr").toString();

        if (group.containsKey("tc"))
            tg.facility = group.get("tc").toString();

        tg.testProvider = group.get("co").toString();
        tg.issuer = group.get("is").toString();
        tg.identifier = group.get("ci").toString();

        return tg;
    }

    private static RecoveryGroup parseRecoveryGroup(CborObject t) throws CborParseException {
        // For some reason they use an array even tho the spec requires exactly one entry ... always...
        Log.d(TAG, "Parsing RecoveryGroup: " + t.toString());
        CborArray arr = (CborArray)t;

        CborMap group = CborMap.createFromCborByteArray(arr.listValue().get(0).toCborByteArray());
        RecoveryGroup rg = new RecoveryGroup();

        rg.disease = group.get("tg").toString();
        rg.firstPositiveTest = group.get("fr").toString();
        rg.testProvider = group.get("co").toString();
        rg.issuer = group.get("is").toString();
        rg.validFrom = group.get("df").toString();
        rg.validUntil = group.get("du").toString();
        rg.identifier = group.get("ci").toString();

        return rg;
    }

    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        return output;
    }

    public static class EUCertificate {
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
    }

    public static class VaccinationGroup {
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

    public static class TestGroup {
        public String disease;
        public String testProvider;
        public String issuer;
        public String identifier;
        public String testType;
        public String testName;
        public @Nullable String testDevice;
        public String sampleDate;
        public String resultDate;
        public String facility;
    }

    public static class RecoveryGroup {
        public String disease;
        public String firstPositiveTest;
        public String testProvider;
        public String issuer;
        public String validFrom;
        public String validUntil;
        public String identifier;
    }

}
