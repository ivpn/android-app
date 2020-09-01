package net.ivpn.client.rest;

import net.ivpn.client.rest.data.ServersListResponse;
import net.ivpn.client.rest.data.addfunds.InitialPaymentRequestBody;
import net.ivpn.client.rest.data.addfunds.InitialPaymentResponse;
import net.ivpn.client.rest.data.privateemails.GenerateEmailRequestBody;
import net.ivpn.client.rest.data.privateemails.GenerateEmailResponse;
import net.ivpn.client.rest.data.privateemails.PrivateEmailsListRequestBody;
import net.ivpn.client.rest.data.privateemails.PrivateEmailsListResponse;
import net.ivpn.client.rest.data.privateemails.RemovePrivateEmailRequestBody;
import net.ivpn.client.rest.data.privateemails.RemovePrivateEmailResponse;
import net.ivpn.client.rest.data.privateemails.UpdatePrivateEmailRequestBody;
import net.ivpn.client.rest.data.privateemails.UpdatePrivateEmailResponse;
import net.ivpn.client.rest.data.proofs.LocationResponse;
import net.ivpn.client.rest.data.session.DeleteSessionRequestBody;
import net.ivpn.client.rest.data.session.DeleteSessionResponse;
import net.ivpn.client.rest.data.session.SessionNewRequestBody;
import net.ivpn.client.rest.data.session.SessionNewResponse;
import net.ivpn.client.rest.data.session.SessionStatusRequestBody;
import net.ivpn.client.rest.data.session.SessionStatusResponse;
import net.ivpn.client.rest.data.addfunds.NewAccountRequestBody;
import net.ivpn.client.rest.data.addfunds.NewAccountResponse;
import net.ivpn.client.rest.data.subscription.SubscriptionRequestBody;
import net.ivpn.client.rest.data.subscription.SubscriptionResponse;
import net.ivpn.client.rest.data.subscription.ValidateAccountRequestBody;
import net.ivpn.client.rest.data.subscription.ValidateAccountResponse;
import net.ivpn.client.rest.data.wireguard.AddWireGuardPublicKeyRequestBody;
import net.ivpn.client.rest.data.wireguard.AddWireGuardPublicKeyResponse;

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

}