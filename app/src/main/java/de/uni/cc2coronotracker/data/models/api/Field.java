package de.uni.cc2coronotracker.data.models.api;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Field implements Serializable, Parcelable {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("alias")
    @Expose
    private String alias;
    @SerializedName("sqlType")
    @Expose
    private String sqlType;
    @SerializedName("domain")
    @Expose
    private Object domain;
    @SerializedName("defaultValue")
    @Expose
    private Object defaultValue;
    @SerializedName("length")
    @Expose
    private Integer length;
    public final static Creator<Field> CREATOR = new Creator<>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Field createFromParcel(android.os.Parcel in) {
            return new Field(in);
        }

        public Field[] newArray(int size) {
            return (new Field[size]);
        }

    };
    private final static long serialVersionUID = 7479639491788321609L;

    protected Field(android.os.Parcel in) {
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.type = ((String) in.readValue((String.class.getClassLoader())));
        this.alias = ((String) in.readValue((String.class.getClassLoader())));
        this.sqlType = ((String) in.readValue((String.class.getClassLoader())));
        this.domain = in.readValue((Object.class.getClassLoader()));
        this.defaultValue = in.readValue((Object.class.getClassLoader()));
        this.length = ((Integer) in.readValue((Integer.class.getClassLoader())));
    }

    public Field() {
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Field.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("type");
        sb.append('=');
        sb.append(((this.type == null) ? "<null>" : this.type));
        sb.append(',');
        sb.append("alias");
        sb.append('=');
        sb.append(((this.alias == null) ? "<null>" : this.alias));
        sb.append(',');
        sb.append("sqlType");
        sb.append('=');
        sb.append(((this.sqlType == null) ? "<null>" : this.sqlType));
        sb.append(',');
        sb.append("domain");
        sb.append('=');
        sb.append(((this.domain == null) ? "<null>" : this.domain));
        sb.append(',');
        sb.append("defaultValue");
        sb.append('=');
        sb.append(((this.defaultValue == null) ? "<null>" : this.defaultValue));
        sb.append(',');
        sb.append("length");
        sb.append('=');
        sb.append(((this.length == null) ? "<null>" : this.length));
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
        dest.writeValue(type);
        dest.writeValue(alias);
        dest.writeValue(sqlType);
        dest.writeValue(domain);
        dest.writeValue(defaultValue);
        dest.writeValue(length);
    }

    public int describeContents() {
        return 0;
    }

}
