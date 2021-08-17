package de.uni.cc2coronotracker.data.models.api;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Attributes implements Serializable, Parcelable {

    @SerializedName("cases7_per_100k")
    @Expose
    private Double cases7Per100k;
    @SerializedName("BL")
    @Expose
    private String bl;
    @SerializedName("county")
    @Expose
    private String county;
    @SerializedName("last_update")
    @Expose
    private String lastUpdate;
    public final static Creator<Attributes> CREATOR = new Creator<>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Attributes createFromParcel(android.os.Parcel in) {
            return new Attributes(in);
        }

        public Attributes[] newArray(int size) {
            return (new Attributes[size]);
        }

    };
    private final static long serialVersionUID = -4488154072416289173L;

    protected Attributes(android.os.Parcel in) {
        this.cases7Per100k = ((Double) in.readValue((Double.class.getClassLoader())));
        this.bl = ((String) in.readValue((String.class.getClassLoader())));
        this.county = ((String) in.readValue((String.class.getClassLoader())));
        this.lastUpdate = ((String) in.readValue((String.class.getClassLoader())));
    }

    public Double getCases7Per100k() {
        return cases7Per100k;
    }

    public String getBl() {
        return bl;
    }

    public String getCounty() {
        return county;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Attributes.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("cases7Per100k");
        sb.append('=');
        sb.append(((this.cases7Per100k == null) ? "<null>" : this.cases7Per100k));
        sb.append(',');
        sb.append("bl");
        sb.append('=');
        sb.append(((this.bl == null) ? "<null>" : this.bl));
        sb.append(',');
        sb.append("county");
        sb.append('=');
        sb.append(((this.county == null) ? "<null>" : this.county));
        sb.append(',');
        sb.append("lastUpdate");
        sb.append('=');
        sb.append(((this.lastUpdate == null) ? "<null>" : this.lastUpdate));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeValue(cases7Per100k);
        dest.writeValue(bl);
        dest.writeValue(county);
        dest.writeValue(lastUpdate);
    }

    public int describeContents() {
        return 0;
    }

}
