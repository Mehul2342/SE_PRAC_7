package com.example.macos.eventm;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.macos.eventm.activity.Main2Activity;
import com.example.macos.eventm.activity.RegistrationActivity;
import com.example.macos.eventm.model.users;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static String user;
    private EditText editTextEmail, edtEmail, editTextPassword;
    private TextView forgetpass;
    private DatabaseReference mDatabase, mpaswodreset;
    Utility u;
    SessionData session;
    private FirebaseAuth firebaseAuth;
    NetworkInfo networkInfo;
    //private CallbackManager mCallbackManager;
    private DatabaseReference mFirebaseDatabaseUsers;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = LoginActivity.class.getSimpleName();
    Button signInButton, buttonSignIn, textViewSignup;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    int varErrorCount;
    Dialog dialog, dialogsucess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /*android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();*/
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mFirebaseDatabaseUsers = FirebaseDatabase.getInstance().getReference("Users");
        mpaswodreset = FirebaseDatabase.getInstance().getReference("Users");
        networkInfo = connectivityManager.getActiveNetworkInfo();
        firebaseAuth = FirebaseAuth.getInstance();
        buttonSignIn = findViewById(R.id.buttonSignIn);
        textViewSignup = findViewById(R.id.textViewSignup);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        forgetpass = (TextView) findViewById(R.id.forgetpass);
        session = new SessionData(LoginActivity.this);
        buttonSignIn.setOnClickListener(this);
        textViewSignup.setOnClickListener(this);
        //signInButton.setOnClickListener(this);
        forgetpass.setOnClickListener(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addScope(new Scope(Scopes.PLUS_LOGIN))
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        u.hidedialog();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (user.getDisplayName() != null) {
                        users users = new users(user.getUid(), user.getDisplayName(), user.getEmail(), "", "", "", String.valueOf(user.getPhotoUrl()), "");
                        mFirebaseDatabaseUsers.child(user.getUid()).child("User").setValue(users);
                        session.setUserid(user.getUid());
                        session.setprofile(String.valueOf(user.getPhotoUrl()));
                        session.setname(user.getDisplayName());
                        session.setother("other");
                        session.setemail(user.getEmail());
                        Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        u.hidedialog();
                    }
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        u = new Utility();
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.joinfb);
        loginButton.setOnClickListener(this);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //mCallbackManager.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                u.hidedialog();
            }
        }*/
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void ForgotPassword() {
        dialog = new Dialog(LoginActivity.this) {
            public boolean dispatchTouchEvent(MotionEvent event) {
                View view = getCurrentFocus();
                boolean ret = super.dispatchTouchEvent(event);
                if (view instanceof EditText) {
                    View w = getCurrentFocus();
                    int scrcoords[] = new int[2];
                    w.getLocationOnScreen(scrcoords);
                    float x = event.getRawX() + w.getLeft() - scrcoords[0];
                    float y = event.getRawY() + w.getTop() - scrcoords[1];
                    if (event.getAction() == MotionEvent.ACTION_UP
                            && (x < w.getLeft() || x >= w.getRight()
                            || y < w.getTop() || y > w.getBottom())) {
                        InputMethodManager imm = (InputMethodManager) LoginActivity.this
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getWindow()
                                .getCurrentFocus().getWindowToken(), 0);
                    }
                }
                return ret;
            }
        };

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.forgot_password);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setCancelable(false);
        dialog.show();
        /*edtEmail = (EditText) dialog.findViewById(R.id.edtEmail);
        edtLayoutEmail = (TextInputLayout) dialog.findViewById(R.id.edtLayoutEmail);
        LinearLayout btnSubmite = (LinearLayout) dialog.findViewById(R.id.btnSubmite);
        btnSubmite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation(edtEmail);
            }
        });*/
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    u.hidedialog();
                }
                return true;
            }
        });

    }

    public void sucess() {
        dialogsucess = new Dialog(LoginActivity.this) {
            public boolean dispatchTouchEvent(MotionEvent event) {
                View view = getCurrentFocus();
                boolean ret = super.dispatchTouchEvent(event);
                if (view instanceof EditText) {
                    View w = getCurrentFocus();
                    int scrcoords[] = new int[2];
                    w.getLocationOnScreen(scrcoords);
                    float x = event.getRawX() + w.getLeft() - scrcoords[0];
                    float y = event.getRawY() + w.getTop() - scrcoords[1];
                    if (event.getAction() == MotionEvent.ACTION_UP
                            && (x < w.getLeft() || x >= w.getRight()
                            || y < w.getTop() || y > w.getBottom())) {
                        InputMethodManager imm = (InputMethodManager) LoginActivity.this
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getWindow()
                                .getCurrentFocus().getWindowToken(), 0);
                    }
                }
                return ret;
            }
        };

        /*dialogsucess.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogsucess.setContentView(R.layout.foregot_password_sucess);
        dialogsucess.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialogsucess.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialogsucess.setCancelable(false);
        dialogsucess.show();
        Button btnSubmite = (Button) dialogsucess.findViewById(R.id.btnback);
        btnSubmite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogsucess.dismiss();
            }
        });
        dialogsucess.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialogsucess.dismiss();
                }
                return true;
            }
        });*/
    }

   /* private void checkValidation(final EditText edtEmail) {
        int intErrorCount = 0;
        String strEmial = edtEmail.getText().toString();
        if (strEmial.toString().length() <= 0) {
            intErrorCount++;
            edtLayoutEmail.setError("Enter Email..!!");
            edtLayoutEmail.setErrorEnabled(true);
            u.hidedialog();
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText()).matches()) {
            intErrorCount++;
            edtLayoutEmail.setError("Enter Valid Email..!!");
            edtLayoutEmail.setErrorEnabled(true);
        } else {
            edtLayoutEmail.setError("");
            edtLayoutEmail.setErrorEnabled(false);
        }

        if (intErrorCount == 0) {
            u.showProgressDialog(Login_Activity.this);
            FirebaseAuth.getInstance().sendPasswordResetEmail(strEmial)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                u.hidedialog();
                                edtEmail.setEnabled(true);
                                dialog.dismiss();
                                sucess();
                            } else {
                                u.hidedialog();
                                u.showAlert(Login_Activity.this, "Error: " + "This user dose not exist please enter valid email address.. !!");
                            }
                        }
                    });
        }
    }*/

    /*rivate void handleFacebookAccessToken(AccessToken token) {
        u.showProgressDialog(Login_Activity.this);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                        } else {
                            Toast.makeText(Login_Activity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        u.hidedialog();

                    }
                });
    }*/

    private void signIn() {
        varErrorCount = 0;
        final String password = editTextPassword.getText().toString();
        final String email = editTextEmail.getText().toString().trim();
        if (email.toString().length() <= 0) {
            varErrorCount++;
            editTextEmail.setError("Enter Email..!!");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText()).matches()) {
            varErrorCount++;
            editTextEmail.setError("Enter Valid Email..!!");
        } else {
            editTextEmail.setError("");
        }
        if (editTextPassword.getText().toString().length() <= 0) {
            varErrorCount++;
            editTextPassword.setError("Enter Password..!!");

        } else if (editTextPassword.getText().toString().length() < 7) {
            varErrorCount++;
            editTextPassword.setError("Password must be greater than 7..!!");
        } else if (editTextPassword.getText().toString().length() > 32) {
            varErrorCount++;
            editTextPassword.setError("Password must be less than 32..!!");
        } else {
            editTextPassword.setError("");
        }
        if (varErrorCount == 0) {
            u.showProgressDialog(LoginActivity.this);
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                session.setuseremail(email.toString());
                                u.hidedialog();
                                mDatabase.orderByChild("User/email").equalTo(session.getuseremail()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot postSnapShot1 : dataSnapshot.getChildren()) {
                                                for (DataSnapshot postSnapShot : postSnapShot1.getChildren()) {
                                                    if (postSnapShot.getKey().equals("User")) {
                                                        users user = postSnapShot.getValue(users.class);
                                                        session.setUserid(user.getUserId());
                                                        session.setprofile(user.getProfile());
                                                        session.setname(user.getName());
                                                        session.setemail(user.getEmail());
                                                        session.setother("system");
                                                        if (!user.getPassword().equals(password.toString())) {
                                                            mpaswodreset.child(user.getUserId()).child("User").child("password").setValue(password.toString());
                                                        }
                                                        u.hidedialog();
                                                        Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            }
                                        } else {
                                            Log.d("Error", "daat not success get");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d("hrllo", databaseError.toString());
                                    }
                                });
                            } else {
                                u.hidedialog();
                                u.showAlert(LoginActivity.this, "There is no user recored corresponding to this identifier the user may have been deleted.");
                            }
                        }
                    });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View view) {
        checkconnectivity c = new checkconnectivity(getApplicationContext());
        if (view == buttonSignIn) {
            if (c.connecttivity()) {
                signIn();
            } else {
                Toast.makeText(getApplicationContext(), "Check Internet Connection", Toast.LENGTH_LONG).show();
            }
        }
        if (view == textViewSignup) {
            startActivity(new Intent(this, RegistrationActivity.class));
        }
        if (view == signInButton) {
            if (c.connecttivity()) {
                signIn1();

            } else {
                Toast.makeText(getApplicationContext(), "Check Internet Connection", Toast.LENGTH_LONG).show();
            }

        }
        if (view == forgetpass) {
            if (c.connecttivity()) {
                ForgotPassword();
            } else {
                Toast.makeText(getApplicationContext(), "Check Internet Connection", Toast.LENGTH_LONG).show();
            }
        }
        /*if (view == loginButton) {
            if (c.connecttivity()) {
                loginButton.setReadPermissions("email", "public_profile");
                loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d("TAG", "facebook:onCancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("TAG", "facebook:onError", error);
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Check Internet Connection", Toast.LENGTH_LONG).show();
            }
        }*/
    }

    private void signIn1() {
        u.showProgressDialog(LoginActivity.this);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}