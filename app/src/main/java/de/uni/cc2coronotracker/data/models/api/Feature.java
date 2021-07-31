package de.uni.cc2coronotracker.data.models.api;

import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Feature implements Serializable, Parcelable {

    @SerializedName("attributes")
    @Expose
    private Attributes attributes;
    public final static Creator<Feature> CREATOR = new Creator<Feature>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Feature createFromParcel(android.os.Parcel in) {
            return new Feature(in);
        }

        public Feature[] newArray(int size) {
            return (new Feature[size]);
        }

    };
    private final static long serialVersionUID = 6974015642343305665L;

    protected Feature(android.os.Parcel in) {
        this.attributes = ((Attributes) in.readValue((Attributes.class.getClassLoader())));
    }

    public Feature() {
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Feature.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("attributes");
        sb.append('=');
        sb.append(((this.attributes == null) ? "<null>" : this.attributes));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeValue(attributes);
    }

    public int describeContents() {
        return 0;
    }

}
