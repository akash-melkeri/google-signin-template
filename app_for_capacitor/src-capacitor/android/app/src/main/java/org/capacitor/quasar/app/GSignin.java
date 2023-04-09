package org.capacitor.quasar.app;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

@NativePlugin()
public class GSignin extends Plugin {
  GoogleSignInClient mGoogleSignInClient;

  @PluginMethod()
  public void init(PluginCall call) {
    String message = call.getString("message");
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestEmail()
      .build();
    mGoogleSignInClient = GoogleSignIn.getClient(getContext().getApplicationContext(), gso);
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext().getApplicationContext());
    System.out.println("\n\n\n");
    System.out.println(call.getData());
    System.out.println("\n\n\n");
    // More code here...
    call.success();
  }

  @PluginMethod()
  public void signIn(PluginCall call) {
    // More code here...
    System.out.println(call.getData());
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(signInIntent);
//    handleSignInResult(task);
    try {
      GoogleSignInAccount account = task.getResult(ApiException.class);
//      JSONObject accessTokenObject = getAuthToken(account.getAccount(), true);

//      JSObject authentication = new JSObject();
//      authentication.put("idToken", account.getIdToken());
//      authentication.put(FIELD_ACCESS_TOKEN, accessTokenObject.get(FIELD_ACCESS_TOKEN));
//      authentication.put(FIELD_TOKEN_EXPIRES, accessTokenObject.get(FIELD_TOKEN_EXPIRES));
//      authentication.put(FIELD_TOKEN_EXPIRES_IN, accessTokenObject.get(FIELD_TOKEN_EXPIRES_IN));
      JSObject user = new JSObject();
//      user.put("serverAuthCode", account.getServerAuthCode());
//      user.put("idToken", account.getIdToken());
//      user.put("authentication", authentication);
//
//      user.put("displayName", account.getDisplayName());
      user.put("email", account.getEmail());
//      user.put("familyName", account.getFamilyName());
//      user.put("givenName", account.getGivenName());
//      user.put("id", account.getId());
//      user.put("imageUrl", account.getPhotoUrl());
      // Signed in successfully, show authenticated UI.
      call.resolve(user);
    } catch (ApiException e) {
      // The ApiException status code indicates the detailed failure reason.
      // Please refer to the GoogleSignInStatusCodes class reference for more information.
      Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
      Log.w(TAG, "signInResult:failed code=" + e.getMessage());
      call.reject("signin failed");
    }
    call.resolve();
  }
}
