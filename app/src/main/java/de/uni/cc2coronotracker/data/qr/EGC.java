package de.uni.cc2coronotracker.data.qr;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.iot.cbor.CborConversionException;
import com.google.iot.cbor.CborParseException;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import COSE.CoseException;
import de.uni.cc2coronotracker.data.models.EUCertificate;
import de.uni.cc2coronotracker.helper.EGCHelper;

public class EGC implements QrIntent.Intent {

    // Raw string as received by scanning the QR code
    public String raw;

    public String issuer;
    public String expiration;
    public String issuedAt;

    public List<EUCertificate> certificates;

    public EGC() {
    }

    public static EGC parse(String toParse) throws IOException, DataFormatException, CoseException, CborParseException, CborConversionException {
        return EGCHelper.parse(toParse);
    }

    @NonNull
    @Override
    public String toString() {
        return TextUtils.join(QrIntent.SEPARATOR, new String[]{
                String.valueOf(QrIntent.QR_EGC),
                issuer,
                expiration,
                issuedAt,
                "#Certs: " + ((certificates != null) ? certificates.size() : 0)
        });
    }
}
