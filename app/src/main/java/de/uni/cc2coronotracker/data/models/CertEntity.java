package de.uni.cc2coronotracker.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import de.uni.cc2coronotracker.data.qr.EGC;

/**
 * Stores the raw QR information of a eu health certificate as well as some metadata for fast show-and-tell
 */
@Entity(tableName = "certificates",
    indices = {@Index(value = {"identifier"}, unique = true)}
)
public class CertEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    // Raw QR contents
    public String raw;

    // Expiration date
    public String expiration;

    // issue date
    @ColumnInfo(name = "issued_at")
    public String issuedAt;

    // As each cert holds exactly one group, this should be set to the appropriate group (e.g. Test)
    @ColumnInfo(name = "group")
    public String groupName;

    // unique cert identifier as stored in the health certificates group
    public String identifier;

    @ColumnInfo(name = "issued_for")
    public String issuedFor;


    public static CertEntity fromEGC(EGC egc) {
        if (egc == null) return null;
        CertEntity cert = new CertEntity();

        cert.expiration = egc.expiration;
        cert.issuedAt = egc.issuedAt;
        cert.expiration = egc.expiration;
        cert.raw = egc.raw;

        cert.groupName = "None";

        if (egc.certificates != null && !egc.certificates.isEmpty())
        {
            EUCertificate euCertificate = egc.certificates.get(0);
            cert.issuedFor = (euCertificate.forenames != null) ?
                    String.format("%s, %s", euCertificate.forenames, euCertificate.surnames) :
                    euCertificate.surnames
            ;

            if (euCertificate.recoveryGroup != null) {
                cert.groupName = "recovery";
                cert.identifier = euCertificate.recoveryGroup.identifier;
            } else if (euCertificate.testGroup != null) {
                cert.groupName = "test";
                cert.identifier = euCertificate.testGroup.identifier;
            } else if (euCertificate.vaccinationGroup != null) {
                cert.groupName = "prophylaxis";
                cert.identifier = euCertificate.vaccinationGroup.identifier;
            } else {
                throw new IllegalArgumentException("Invalid EGC: Must contain a valid health certificate group.");
            }
        } else {
            throw new IllegalArgumentException("Invalid EGC: Must contain a valid health certificate.");
        }
        return cert;
    }
}
