package de.uni.cc2coronotracker.data.qr;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.google.android.gms.common.util.Hex;
import com.upokecenter.cbor.CBORObject;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import COSE.CoseException;
import COSE.Message;
import COSE.MessageTag;
import COSE.Sign1Message;
import nl.minvws.encoding.Base45;

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

    public static Intent fromString(@NonNull String toParse) throws InstantiationException, CoseException, IOException, DataFormatException {
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

        public EGC() {

        }

        public static EGC parse(String toParse) throws IOException, DataFormatException, CoseException {
            byte[] decodedBytes = Base45.getDecoder().decode(toParse.substring(4));
            byte[] decompressed = decompress(decodedBytes);


            Sign1Message msg = (Sign1Message) Message.DecodeFromBytes(decompressed, MessageTag.Sign1);
            byte[] content = msg.GetContent();

            Log.d(TAG, Hex.bytesToStringLowercase(decompressed));
            Log.d(TAG, Hex.bytesToStringLowercase(content));

            CBORObject cborObject = CBORObject.DecodeFromBytes(content);
            Log.d(TAG, cborObject.ToJSONString());

            return new EGC();
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

        @Override
        public String toString() {
            return TextUtils.join(SEPARATOR, new String[] {
                    String.valueOf(QR_EGC)
            });
        }
    }

}
