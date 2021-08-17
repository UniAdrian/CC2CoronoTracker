package de.uni.cc2coronotracker.data.models.api;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UniqueIdField implements Serializable, Parcelable {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("isSystemMaintained")
    @Expose
    private Boolean isSystemMaintained;
    public final static Creator<UniqueIdField> CREATOR = new Creator<>() {


        @SuppressWarnings({
                "unchecked"
        })
        public UniqueIdField createFromParcel(android.os.Parcel in) {
            return new UniqueIdField(in);
        }

        public UniqueIdField[] newArray(int size) {
            return (new UniqueIdField[size]);
        }

    };
    private final static long serialVersionUID = 5134924443187940764L;

    protected UniqueIdField(android.os.Parcel in) {
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.isSystemMaintained = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(UniqueIdField.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("isSystemMaintained");
        sb.append('=');
        sb.append(((this.isSystemMaintained == null) ? "<null>" : this.isSystemMaintained));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeValue(name);
        dest.writeValue(isSystemMaintained);
    }

    public int describeContents() {
        return 0;
    }

}
