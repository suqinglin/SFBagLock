package com.nexless.sfbaglock.http;

import com.nexless.sfbaglock.bean.LoginResponse;
import com.nexless.sfbaglock.bean.ProjectsResponse;
import com.nexless.sfbaglock.bean.TResponse;
import com.nexless.sfbaglock.bean.UploadCsvResponse;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * @date: 2019/5/23
 * @author: su qinglin
 * @description:
 */
public interface ApiService {

    @FormUrlEncoded
    @POST("/bll-admin/open-api/login")
    Observable<TResponse<LoginResponse>> login(
            @Field("userPhone") String userPhone,
            @Field("password") String password
    );

    @POST("/bll-admin/open-api/projects")
    Observable<TResponse<ProjectsResponse>> getProjects(
    );

    @Multipart
    @POST("/bll-admin/open-api/box/import")
    Observable<TResponse<UploadCsvResponse>> uploadCsv(@Part MultipartBody.Part file);
}
