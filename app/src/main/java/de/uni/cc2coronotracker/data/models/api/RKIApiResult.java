package de.uni.cc2coronotracker.data.models.api;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class RKIApiResult implements Serializable, Parcelable
{

    @SerializedName("objectIdFieldName")
    @Expose
    private String objectIdFieldName;
    @SerializedName("uniqueIdField")
    @Expose
    private UniqueIdField uniqueIdField;
    @SerializedName("globalIdFieldName")
    @Expose
    private String globalIdFieldName;
    @SerializedName("geometryType")
    @Expose
    private String geometryType;
    @SerializedName("spatialReference")
    @Expose
    private SpatialReference spatialReference;
    @SerializedName("fields")
    @Expose
    private List<Field> fields = new ArrayList<>();
    @SerializedName("features")
    @Expose
    private List<Feature> features = new ArrayList<>();
    public final static Creator<RKIApiResult> CREATOR = new Creator<>() {


        @SuppressWarnings({
                "unchecked"
        })
        public RKIApiResult createFromParcel(android.os.Parcel in) {
            return new RKIApiResult(in);
        }

        public RKIApiResult[] newArray(int size) {
            return (new RKIApiResult[size]);
        }

    }
            ;
    private final static long serialVersionUID = 6419507486693283857L;

    protected RKIApiResult(android.os.Parcel in) {
        this.objectIdFieldName = ((String) in.readValue((String.class.getClassLoader())));
        this.uniqueIdField = ((UniqueIdField) in.readValue((UniqueIdField.class.getClassLoader())));
        this.globalIdFieldName = ((String) in.readValue((String.class.getClassLoader())));
        this.geometryType = ((String) in.readValue((String.class.getClassLoader())));
        this.spatialReference = ((SpatialReference) in.readValue((SpatialReference.class.getClassLoader())));
        in.readList(this.fields, (Field.class.getClassLoader()));
        in.readList(this.features, (Feature.class.getClassLoader()));
    }

    public List<Feature> getFeatures() {
        return features;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RKIApiResult.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("objectIdFieldName");
        sb.append('=');
        sb.append(((this.objectIdFieldName == null)?"<null>":this.objectIdFieldName));
        sb.append(',');
        sb.append("uniqueIdField");
        sb.append('=');
        sb.append(((this.uniqueIdField == null)?"<null>":this.uniqueIdField));
        sb.append(',');
        sb.append("globalIdFieldName");
        sb.append('=');
        sb.append(((this.globalIdFieldName == null)?"<null>":this.globalIdFieldName));
        sb.append(',');
        sb.append("geometryType");
        sb.append('=');
        sb.append(((this.geometryType == null)?"<null>":this.geometryType));
        sb.append(',');
        sb.append("spatialReference");
        sb.append('=');
        sb.append(((this.spatialReference == null)?"<null>":this.spatialReference));
        sb.append(',');
        sb.append("fields");
        sb.append('=');
        sb.append(((this.fields == null)?"<null>":this.fields));
        sb.append(',');
        sb.append("features");
        sb.append('=');
        sb.append(((this.features == null)?"<null>":this.features));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeValue(objectIdFieldName);
        dest.writeValue(uniqueIdField);
        dest.writeValue(globalIdFieldName);
        dest.writeValue(geometryType);
        dest.writeValue(spatialReference);
        dest.writeList(fields);
        dest.writeList(features);
    }

    public int describeContents() {
        return 0;
    }

}


