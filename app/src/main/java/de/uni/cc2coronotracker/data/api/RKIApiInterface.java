package de.uni.cc2coronotracker.data.api;

import de.uni.cc2coronotracker.data.models.api.RKIApiResult;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RKIApiInterface {
    @GET("query?where=1=1&outFields=cases7_per_100k,BL,county,last_update&geometryType=esriGeometryPoint&inSR=4326&f=json&returnGeometry=false")
    Call<RKIApiResult> getIncidenceByLocation(@Query("geometry") String longLat);
}
