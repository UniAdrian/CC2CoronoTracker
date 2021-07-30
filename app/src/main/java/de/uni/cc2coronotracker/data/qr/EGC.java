package de.uni.cc2coronotracker.data.qr;

import android.text.TextUtils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.iot.cbor.CborConversionException;
import com.google.iot.cbor.CborParseException;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import COSE.CoseException;
import de.uni.cc2coronotracker.data.models.EUCertificate;
import de.uni.cc2coronotracker.helper.EGCHelper;

@Entity(tableName="certificates")
public class EGC implements QrIntent.Intent {

    @PrimaryKey(autoGenerate = true)
    public long id;

    // Raw string as received by scanning the QR code
    public String raw;

    public String issuer;
    public String expiration;
    @ColumnInfo(name = "issued_at")
    public String issuedAt;

    @Ignore()
    public List<EUCertificate> certificates;

    public EGC() {
    }

    public static EGC parse(String toParse) throws IOException, DataFormatException, CoseException, CborParseException, CborConversionException {
        return EGCHelper.parse(toParse);
    }

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
