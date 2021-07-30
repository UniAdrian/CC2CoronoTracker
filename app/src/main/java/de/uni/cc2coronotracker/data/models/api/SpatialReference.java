package de.uni.cc2coronotracker.data.models.api;

import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SpatialReference implements Serializable, Parcelable {

    @SerializedName("wkid")
    @Expose
    private Integer wkid;
    @SerializedName("latestWkid")
    @Expose
    private Integer latestWkid;
    public final static Creator<SpatialReference> CREATOR = new Creator<SpatialReference>() {


        @SuppressWarnings({
                "unchecked"
        })
        public SpatialReference createFromParcel(android.os.Parcel in) {
            return new SpatialReference(in);
        }

        public SpatialReference[] newArray(int size) {
            return (new SpatialReference[size]);
        }

    };
    private final static long serialVersionUID = -1511370038718545334L;

    protected SpatialReference(android.os.Parcel in) {
        this.wkid = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.latestWkid = ((Integer) in.readValue((Integer.class.getClassLoader())));
    }

    public SpatialReference() {
    }

    public Integer getWkid() {
        return wkid;
    }

    public void setWkid(Integer wkid) {
        this.wkid = wkid;
    }

    public Integer getLatestWkid() {
        return latestWkid;
    }

    public void setLatestWkid(Integer latestWkid) {
        this.latestWkid = latestWkid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SpatialReference.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("wkid");
        sb.append('=');
        sb.append(((this.wkid == null) ? "<null>" : this.wkid));
        sb.append(',');
        sb.append("latestWkid");
        sb.append('=');
        sb.append(((this.latestWkid == null) ? "<null>" : this.latestWkid));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeValue(wkid);
        dest.writeValue(latestWkid);
    }

    public int describeContents() {
        return 0;
    }

}
