package de.uni.cc2coronotracker.data.qr;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.google.iot.cbor.CborConversionException;
import com.google.iot.cbor.CborParseException;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.UUID;
import java.util.zip.DataFormatException;

import COSE.CoseException;
import de.uni.cc2coronotracker.helper.EGCHelper;

/**
 * A QR intent is a bundle of an identifier and some additional parameters
 * Currently uses very rudimentary parsing that is neither efficient nor very flexible.
 *
 * If more complex QR Types should arise it is recommended to change the parsing pattern to a more
 * efficient, flexible and robust scheme.
 *
 * TODO: IMPLEMENT PROPER ERROR HANDLING WITH MEANINGFUL MESSAGES
 */
public class QrIntent {

    private static final String TAG = "QrIntent";

    public interface Intent extends Serializable {}

    public static final int QR_ADD_EXPOSURE = 0;
    public static final int QR_IMPORT       = 1;
    public static final int QR_EGC          = 2;

    public static final String EGC_PREFIX = "HC1";

    /**
     * It is recommended to use this approach over an enum to keep dex sizes smalls.
     * Doesnt matter much for one enum but i like to stick with good practices.
     */
    @IntDef({QR_ADD_EXPOSURE, QR_IMPORT, QR_EGC})
    @Retention(RetentionPolicy.SOURCE)
    public @interface QR_INTENT { }


    public final static String SEPARATOR = ";";

    public static Intent fromString(@NonNull String toParse) throws InstantiationException, CoseException, IOException, DataFormatException, CborParseException, CborConversionException {
        if (StringUtils.isBlank(toParse)) {
            throw new IllegalArgumentException("The provided code is malformed: The code cannot be blank.");
        }

        if (toParse.startsWith(EGC_PREFIX)) {
            return EGC.parse(toParse);
        }

        String[] frags = toParse.split(SEPARATOR);
        int intentType = Integer.valueOf(frags[0]);

        switch (intentType) {
            case QR_ADD_EXPOSURE:
                return AddExposure.parse(frags);

            case QR_IMPORT:
                return ImportSettings.parse(frags);

            default:
                Log.e(TAG, "Could not parse: \n" + toParse);
                throw new InstantiationException("The provided code is malformed: Unknown intent identifier.");
        }
    }

    public static class AddExposure implements Intent {
        public final UUID uuid;
        public final boolean allowTracking;

        public AddExposure(UUID uuid, boolean allowTracking) {
            this.uuid = uuid;
            this.allowTracking = allowTracking;
        }

        /**
         * @param params String array of msgId [, additional params]
         * @return The AddExposure intent, if valid
         */
        public static AddExposure parse(String[] params) {

            if (params.length != 3) {
                throw new IllegalArgumentException("The provided code is malformed: Wrong number of parameters.");
            }

            UUID uuid = UUID.fromString(params[1]);
            boolean allowTracking = Boolean.valueOf(params[2]);

            return new AddExposure(uuid, allowTracking);
        }

        @Override
        public String toString() {
            return TextUtils.join(SEPARATOR, new String[] {
                    String.valueOf(QR_ADD_EXPOSURE),
                    uuid.toString(),
                    String.valueOf(allowTracking)
            });
        }
    }


    public static class ImportSettings implements Intent {
        public final UUID uuid;
        public final boolean allowTracking;
        public final boolean doTrack;

        public ImportSettings(UUID uuid, boolean allowTracking, boolean doTrack) {
            this.uuid = uuid;
            this.allowTracking = allowTracking;
            this.doTrack = doTrack;
        }

        /**
         * @param params String array of msgId [, additional params]
         * @implNote If preferences become more complex pull parsing preferences into its own method and bundle it
         * @return The AddExposure intent, if valid
         */
        public static ImportSettings parse(String[] params) {

            if (params.length != 4) {
                throw new IllegalArgumentException("The provided code is malformed: Wrong number of parameters.");
            }

            UUID uuid = UUID.fromString(params[1]);
            boolean allowTracking = Boolean.valueOf(params[2]);
            boolean doTrack = Boolean.valueOf(params[3]);

            return new ImportSettings(uuid, allowTracking, doTrack);
        }

        @Override
        public String toString() {
            return TextUtils.join(SEPARATOR, new String[] {
                    String.valueOf(QR_IMPORT),
                    uuid.toString(),
                    String.valueOf(allowTracking),
                    String.valueOf(doTrack)
            });
        }
    }

    public static class EGC implements Intent {

        // Raw string as received by scanning the QR code
        public String raw;

        public String issuer;
        public Long expiration;
        public Long issuedAt;

        public List<EGCHelper.EUCertificate> certificates;

        public EGC() {
        }

        public static EGC parse(String toParse) throws IOException, DataFormatException, CoseException, CborParseException, CborConversionException {
            return EGCHelper.parse(toParse);
        }

        @Override
        public String toString() {
            return TextUtils.join(SEPARATOR, new String[] {
                    String.valueOf(QR_EGC),
                    issuer,
                    expiration.toString(),
                    issuedAt.toString(),
                    "#Certs: " + ((certificates != null) ? certificates.size() : 0)
            });
        }
    }

}
