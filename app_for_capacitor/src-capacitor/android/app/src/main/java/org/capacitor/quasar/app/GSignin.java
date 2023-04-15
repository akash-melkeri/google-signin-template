package org.capacitor.quasar.app;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PluginResult;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

@NativePlugin(requestCodes={111})
public class GSignin extends Plugin {
//  private PluginCall pluginCall;
  GoogleSignInClient mGoogleSignInClient;
  private FirebaseAuth mAuth;
  private BeginSignInRequest signInRequest;
  final private int GSCODE = 111;
  private String serverId = "1043939121137-mq1c0o76mus1792drhsmhrr68s2eli5c.apps.googleusercontent.com";


  @Override
  public void load() {
    System.out.println("\n\n\nhelllllllllllllllooooooooooo\n\n\n");
  }
  @PluginMethod()
  public void init(PluginCall call) {
    saveCall(call);
    mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    if(currentUser != null){
      currentUser.getIdToken(true)
        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
          public void onComplete(@NonNull Task<GetTokenResult> task) {
            if (task.isSuccessful()) {
              String idToken = task.getResult().getToken();
              call.resolve(getAccountJson(idToken));
              // Send token to your backend via HTTPS
              // ...
            } else {
              // Handle error -> task.getException();
              call.reject("failed");
            }
          }
        });
      return;
//      call.resolve(getAccountJson(currentUser));
    }
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(serverId)
      .build();
    mGoogleSignInClient = GoogleSignIn.getClient(getContext().getApplicationContext(), gso);
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext().getApplicationContext());
    if(account != null){
      call.resolve(getAccountJson(account));
      return;
    }
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    startActivityForResult(call,signInIntent, GSCODE);
  }
  @PluginMethod()
  public void signIn(PluginCall call) {
    call.resolve();
  }
  @Override
  public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
    super.handleOnActivityResult(requestCode, resultCode, data);
    PluginCall pluginCall = getSavedCall();
    if (requestCode == GSCODE) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        GoogleSignInAccount account = task.getResult(ApiException.class);
        String idToken = account.getIdToken();
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
          .addOnCompleteListener(getContext().getMainExecutor(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
              if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success");
                FirebaseUser user = mAuth.getCurrentUser();
                user.getIdToken(true)
                  .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                      if (task.isSuccessful()) {
                        String idToken = task.getResult().getToken();
                        pluginCall.resolve(getAccountJson(idToken));
                        // Send token to your backend via HTTPS
                        // ...
                      } else {
                        // Handle error -> task.getException();
                        pluginCall.reject("failed");
                      }
                    }
                  });
              } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                pluginCall.reject("failed");
              }
            }
          });
      } catch (ApiException e) {
        Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        pluginCall.reject("failed");
      }

    }
  }


  private JSObject getAccountJson(String token) {
    JSObject response = new JSObject();
    response.put("status",true);
    response.put("token",token);
    return response;
  }
  private JSObject getAccountJson(GoogleSignInAccount account) {
    JSObject response = new JSObject();
    response.put("status",true);
    response.put("token",account.getIdToken());
    return response;
  }
  private JSObject getAccountJson() {
    JSObject response = new JSObject();
    response.put("status",false);
    return response;
  }
  @PluginMethod
  public void signOut(PluginCall call) {
    FirebaseAuth.getInstance().signOut();
    signOutGoogle();
    JSObject response = new JSObject();
    response.put("success",true);
    call.resolve(response);
  }
  private void signOutGoogle() {
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(serverId)
      .build();
    GoogleSignIn.getClient(getContext().getApplicationContext(),gso).signOut()
      .addOnCompleteListener(getContext().getMainExecutor(), new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
          // ...
        }
      });
  }
}
