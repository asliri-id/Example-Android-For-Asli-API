package com.yusuffirdaus.aslirisalesdemo.api;



import com.yusuffirdaus.aslirisalesdemo.model.DataParamFaceCrop;
import com.yusuffirdaus.aslirisalesdemo.model.DataParamOcrExtra;
import com.yusuffirdaus.aslirisalesdemo.model.DataParamProfesional;
import com.yusuffirdaus.aslirisalesdemo.model.FaceCrop;
import com.yusuffirdaus.aslirisalesdemo.model.Liveness;
import com.yusuffirdaus.aslirisalesdemo.model.Ocr;
import com.yusuffirdaus.aslirisalesdemo.model.Profesional;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface APIrequestDdata {



    @POST("ocr_extra")
    Call<Ocr> ardOcrExtra(@HeaderMap Map<String, String> headers, @Body DataParamOcrExtra pm);
    @POST("verify_ocr")
    Call<Ocr> ardOcr(@HeaderMap Map<String, String> headers, @Body DataParamOcrExtra pm);

    @POST("auto_face_crop")
    Call<FaceCrop> ardFaceCrop(@HeaderMap Map<String, String> headers, @Body DataParamFaceCrop pm);

    @POST("verify_profesional")
    Call<Profesional> ardPro(@HeaderMap Map<String, String> headers, @Body DataParamProfesional pm);

    @Multipart
    @POST("liveness")
    Call<Liveness> ardLiveness(@HeaderMap Map<String, String> headers, @Part List<MultipartBody.Part> files, @PartMap() Map<String, RequestBody> partMap);

}
