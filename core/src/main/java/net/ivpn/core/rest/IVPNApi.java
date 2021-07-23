package net.ivpn.core.rest;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import net.ivpn.core.rest.data.ServersListResponse;
import net.ivpn.core.rest.data.addfunds.AddFundsRequestBody;
import net.ivpn.core.rest.data.addfunds.AddFundsResponse;
import net.ivpn.core.rest.data.addfunds.InitialPaymentRequestBody;
import net.ivpn.core.rest.data.addfunds.InitialPaymentResponse;
import net.ivpn.core.rest.data.addfunds.NewAccountRequestBody;
import net.ivpn.core.rest.data.addfunds.NewAccountResponse;
import net.ivpn.core.rest.data.proofs.LocationResponse;
import net.ivpn.core.rest.data.session.DeleteSessionRequestBody;
import net.ivpn.core.rest.data.session.DeleteSessionResponse;
import net.ivpn.core.rest.data.session.SessionNewRequestBody;
import net.ivpn.core.rest.data.session.SessionNewResponse;
import net.ivpn.core.rest.data.session.SessionStatusRequestBody;
import net.ivpn.core.rest.data.session.SessionStatusResponse;
import net.ivpn.core.rest.data.subscription.SubscriptionRequestBody;
import net.ivpn.core.rest.data.subscription.SubscriptionResponse;
import net.ivpn.core.rest.data.subscription.ValidateAccountRequestBody;
import net.ivpn.core.rest.data.subscription.ValidateAccountResponse;
import net.ivpn.core.rest.data.wireguard.AddWireGuardPublicKeyRequestBody;
import net.ivpn.core.rest.data.wireguard.AddWireGuardPublicKeyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface IVPNApi {

    @GET("v4/servers.json")
    Call<ServersListResponse> getServers();

    @GET("v4/geo-lookup")
    Call<LocationResponse> getLocation();
    
    @POST("v4/session/new")
    Call<SessionNewResponse> newSession(@Body SessionNewRequestBody body);

    @POST("v4/session/status")
    Call<SessionStatusResponse> sessionStatus(@Body SessionStatusRequestBody body);

    @POST("v4/session/delete")
    Call<DeleteSessionResponse> deleteSession(@Body DeleteSessionRequestBody body);

    @POST("v4/session/wg/set")
    Call<AddWireGuardPublicKeyResponse> setWireGuardPublicKey(@Body AddWireGuardPublicKeyRequestBody body);

    //old logic for subscriptions
    @POST("/subscriptions/android")
    Call<SubscriptionResponse> processPurchase(@Body SubscriptionRequestBody body);

    @POST("/subscriptions/validate")
    Call<ValidateAccountResponse> validateAccount(@Body ValidateAccountRequestBody body);

    //new add funds logic
    @POST("v4/account/new")
    Call<NewAccountResponse> newAccount(@Body NewAccountRequestBody body);

    @POST("/v4/account/payment/android/initial")
    Call<InitialPaymentResponse> initialPayment(@Body InitialPaymentRequestBody body);

    @POST("/v4/account/payment/android/add")
    Call<AddFundsResponse> addFunds(@Body AddFundsRequestBody body);

}