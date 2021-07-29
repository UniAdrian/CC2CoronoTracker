package de.uni.cc2coronotracker.helper;

import android.util.Log;

import com.google.android.gms.common.util.Hex;
import com.google.iot.cbor.CborArray;
import com.google.iot.cbor.CborConversionException;
import com.google.iot.cbor.CborMap;
import com.google.iot.cbor.CborObject;
import com.google.iot.cbor.CborParseException;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import COSE.CoseException;
import COSE.Message;
import COSE.MessageTag;
import COSE.Sign1Message;
import de.uni.cc2coronotracker.data.models.EUCertificate;
import de.uni.cc2coronotracker.data.qr.EGC;
import nl.minvws.encoding.Base45;

public class EGCHelper {

    private static final String TAG = "EGCHelper";


    public static EGC parse(String toParse) throws CborConversionException, CborParseException, IOException, DataFormatException, CoseException {

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

                euCert.version = getString(cert, "ver");

                CborMap names = CborMap.createFromCborByteArray(cert.get("nam").toCborByteArray());
                euCert.surnames = getString(names, "fn");
                euCert.surnamesStandardised = getString(names, "fnt");

                euCert.forenames = getString(names, "gn", null);
                euCert.forenamesStandardised = getString(names, "gnt", null);

                euCert.dateOfBirth = getString(cert, "dob");


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

        EGC egc = new EGC();
        egc.raw = toParse;
        egc.issuer = StringUtils.unwrap(issuer.toString(), '"');

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long expiredAtLong = Long.parseLong(StringUtils.unwrap(expirationTime.toString(), '"'));
        long issuedAtLong = Long.parseLong(StringUtils.unwrap(issuedAt.toString(), '"'));

        egc.expiration = sf.format(new Date(expiredAtLong * 1000));
        egc.issuedAt = sf.format(new Date(issuedAtLong * 1000));

        egc.certificates = parsedCerts;

        return egc;
    }

    private static EUCertificate.VaccinationGroup parseVaccinationGroup(CborObject v) throws CborParseException {
        // For some reason they use an array even tho the spec requires exactly one entry ... always...
        Log.d(TAG, "Parsing VaccineGroup: " + v.toString());
        CborArray arr = (CborArray)v;

        CborMap group = CborMap.createFromCborByteArray(arr.listValue().get(0).toCborByteArray());
        EUCertificate.VaccinationGroup vg = new EUCertificate.VaccinationGroup();

        vg.disease = getString(group,"tg");
        vg.prophylaxis = getString(group,"vp");

        vg.product = getString(group,"mp");
        vg.producer = getString(group,"ma");

        vg.dose = getString(group,"dn");
        vg.doseRequired = getString(group,"sd");
        vg.doseReceived = getString(group,"dt");
        vg.doseProvider = getString(group,"co");

        vg.issuer = getString(group,"is");
        vg.identifier = getString(group,"ci");

        return vg;
    }

    private static EUCertificate.TestGroup parseTestGroup(CborObject t) throws CborParseException {
        // For some reason they use an array even tho the spec requires exactly one entry ... always...
        Log.d(TAG, "Parsing TestGroup: " + t.toString());
        CborArray arr = (CborArray)t;

        CborMap group = CborMap.createFromCborByteArray(arr.listValue().get(0).toCborByteArray());
        EUCertificate.TestGroup tg = new EUCertificate.TestGroup();

        tg.disease = getString(group,"tg");
        tg.testType = getString(group,"tt");
        tg.testName = getString(group,"nm", null);
        tg.testDevice = getString(group,"ma", null);

        tg.sampleDate = getString(group,"sc");
        tg.result = getString(group,"tr");

        tg.facility = getString(group,"tc", null);

        tg.testProvider = getString(group,"co");
        tg.issuer = getString(group,"is");
        tg.identifier = getString(group,"ci");

        return tg;
    }

    private static EUCertificate.RecoveryGroup parseRecoveryGroup(CborObject t) throws CborParseException {
        // For some reason they use an array even tho the spec requires exactly one entry ... always...
        Log.d(TAG, "Parsing RecoveryGroup: " + t.toString());
        CborArray arr = (CborArray)t;

        CborMap group = CborMap.createFromCborByteArray(arr.listValue().get(0).toCborByteArray());
        EUCertificate.RecoveryGroup rg = new EUCertificate.RecoveryGroup();

        rg.disease = getString(group,"tg");
        rg.firstPositiveTest = getString(group,"fr");
        rg.testProvider = getString(group,"co");
        rg.issuer = getString(group,"is");
        rg.validFrom = getString(group,"df");
        rg.validUntil = getString(group,"du");
        rg.identifier = getString(group,"ci");

        return rg;
    }

    private static String getString(CborMap obj, String key) {
        if (obj.containsKey(key))
            return StringUtils.unwrap(obj.get(key).toString(), '"');

        throw new IllegalArgumentException("Expected map to have key '" + key + "', but was not found.");
    }

    private static String getString(CborMap obj, String key, String defaultVal) {
        if (obj.containsKey(key))
            return StringUtils.unwrap(obj.get(key).toString(), '"');

        return defaultVal;
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

}
