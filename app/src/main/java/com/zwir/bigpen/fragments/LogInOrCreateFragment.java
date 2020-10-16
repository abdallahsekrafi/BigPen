package com.zwir.bigpen.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.text.InputType;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zwir.bigpen.R;
import com.zwir.bigpen.activitys.BookRepository;
import com.zwir.bigpen.customs.BigPenProgressDialog;
import com.zwir.bigpen.customs.BigPenToast;
import com.zwir.bigpen.util.Constants;
import com.zwir.bigpen.data.LocalPreferences;


public class LogInOrCreateFragment extends DialogFragment {

    private final String CONNEXION = "connexion", CREATE_ACCOUNT = "create", RESET_PWD = "reset";
    private LocalPreferences localPreferences;
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputPassword;
    private TextInputLayout textInputUserName;
    private TextView navigationMode;
    private TextView resetPwd;
    private LinearLayout passwordLayout;
    private Button btnCon;
    private Boolean hideMode = true;
    private String logInOrCreate = CONNEXION;
    private FirebaseAuth fireBaseAuth;
    private String actionResult;
    private BookRepository bookRepository;
    // create an AlertDialog and return it
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        localPreferences=new LocalPreferences(getContext());
        fireBaseAuth=FirebaseAuth.getInstance();
        actionResult =getArguments().getString(Constants.actionString);
        if (!actionResult.equals(Constants.uploadProject))
            bookRepository = (BookRepository) getActivity();
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(), R.style.AppDialogStyle);
        View loginOrCreateDialog = getActivity().getLayoutInflater().inflate(
                R.layout.fragment_log_in_or_create, null);
        View customTitle = getActivity().getLayoutInflater().inflate(
                R.layout.custom_dialog_title, null);
        customTitle.findViewById(R.id.dialog_icon).setBackgroundResource(R.drawable.ic_account);
        ((TextView)customTitle.findViewById(R.id.dialog_title)).setText(R.string.title_login_or_create_dialog);
        builder.setView(loginOrCreateDialog); // add GUI to dialog
        // set the AlertDialog's message
        builder.setCustomTitle(customTitle);
        // get button
        textInputEmail = loginOrCreateDialog.findViewById(R.id.text_input_email);
        textInputEmail.getEditText().setText(localPreferences.getUserCachedEmail());
        textInputEmail.getEditText().setSelection(textInputEmail.getEditText().getText().length());
        textInputUserName = loginOrCreateDialog.findViewById(R.id.text_input_name);
        textInputPassword = loginOrCreateDialog.findViewById(R.id.text_input_pwd);
        btnCon = loginOrCreateDialog.findViewById(R.id.button_con);
        navigationMode = loginOrCreateDialog.findViewById(R.id.text_create_account);
        resetPwd = loginOrCreateDialog.findViewById(R.id.text_reset_password);
        passwordLayout = loginOrCreateDialog.findViewById(R.id.password_layout);
        // button actionResult listener
        loginOrCreateDialog.findViewById(R.id.button_hide_look_pw).setOnClickListener(hideLookPWListener);
        btnCon.setOnClickListener(btnConListener);
        navigationMode.setOnClickListener(navigationModeListener);
        resetPwd.setOnClickListener(forgotPWListener);
        // keyboard actionResult listener
        textInputEmail.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    switch (logInOrCreate) {
                        case RESET_PWD:
                            loginOrCreate();
                            break;
                        case CONNEXION:
                            textInputPassword.requestFocus();
                            break;
                        case CREATE_ACCOUNT:
                            textInputUserName.requestFocus();
                            break;
                    }
                }
                return false;
            }
        });
        textInputUserName.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    textInputPassword.requestFocus();
                }
                return false;
            }
        });
        textInputPassword.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    loginOrCreate();
                }
                return false;
            }
        });
        return builder.create(); // return dialog
    }

    private void loginOrCreate() {
        final String emailInput = textInputEmail.getEditText().getText().toString().trim();
        final String userNameInput = textInputUserName.getEditText().getText().toString().trim();
        final String passwordInput = textInputPassword.getEditText().getText().toString().trim();
        final BigPenProgressDialog bigPenProgressDialog=new BigPenProgressDialog(getContext(),getString(R.string.please_wait));
        switch (logInOrCreate) {
            case RESET_PWD:
                if (validateEmail(emailInput)) {
                    bigPenProgressDialog.showMe();
                    fireBaseAuth.sendPasswordResetEmail(emailInput)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    bigPenProgressDialog.dismissMe();
                                    if (task.isSuccessful())
                                        new BigPenToast(getContext(), R.drawable.ic_email, R.string.check_your_mailbox);
                                    else
                                        new BigPenToast(getContext(),R.drawable.ic_error,R.string.system_error);
                                }
                            });
                } else return;
                break;
            case CONNEXION:
                if (validateEmail(emailInput) && validatePassword(passwordInput)) {
                    bigPenProgressDialog.showMe();
                    fireBaseAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    bigPenProgressDialog.dismissMe();
                                    if (task.isSuccessful()) {
                                        final FirebaseUser userSignIn = task.getResult().getUser();
                                        if (userSignIn.isEmailVerified()){
                                            localPreferences.setUserCachedEmail(emailInput);
                                            dismiss();
                                            switch (actionResult) {
                                                case Constants.toAccountFrag:
                                                    bookRepository.myAccountFragment();
                                                    break;
                                                case Constants.toProjectFrag:
                                                    bookRepository.myProjectsFragment();
                                                    break;
                                                case Constants.toCartFrag:
                                                    bookRepository.myCartFragment();
                                                    break;
                                                case Constants.toPurchaseFrag:
                                                    bookRepository.myPurchaseFragment();
                                                    break;
                                                case Constants.addBookToCart:
                                                    new BigPenToast(getContext(), R.drawable.ic_account_validate, R.string.message_purchase_book_now);
                                                    break;
                                                case Constants.uploadProject:
                                                    new BigPenToast(getContext(), R.drawable.ic_account_validate, R.string.message_uploading_page_now);
                                                    break;
                                            }
                                        }
                                        else{
                                            fireBaseAuth.signOut();
                                            new BigPenToast(getContext(), R.drawable.ic_account,R.string.activate_your_account);
                                        }

                                    }
                                    else {
                                        new BigPenToast(getContext(), R.drawable.ic_error, R.string.login_failed);
                                        textInputPassword.getEditText().setText("");
                                    }

                                }

                            });
                } else return;
                break;
            case CREATE_ACCOUNT:
                if (validateEmail(emailInput) && validatePassword(passwordInput) && validateUserName(userNameInput)) {
                    bigPenProgressDialog.showMe();
                    fireBaseAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> createdTask) {
                                    if (createdTask.isSuccessful()) {
                                        final FirebaseUser userCreated = createdTask.getResult().getUser();
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(userNameInput)
                                                .build();
                                        userCreated.updateProfile(profileUpdates);
                                        userCreated.sendEmailVerification()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> emailVerificationTask) {
                                                        bigPenProgressDialog.dismissMe();
                                                        if (emailVerificationTask.isSuccessful()) {
                                                            DatabaseReference mRef=FirebaseDatabase.getInstance().getReference(Constants.fireBaseUsers)
                                                                    .child(userCreated.getUid());
                                                            mRef.child(Constants.fireBaseBalance)
                                                                    .setValue(Constants.fireBaseDefaultBalanceValue);
                                                            mRef.child(Constants.fireBaseTotalStorage)
                                                                    .setValue(Constants.fireBaseDefaultStorage);
                                                            mRef.child(Constants.fireBaseUsedStorage)
                                                                    .setValue(0);
                                                            new BigPenToast(getContext(), R.drawable.ic_email, R.string.success_account_create);
                                                            textInputPassword.getEditText().setText("");
                                                            textInputUserName.setVisibility(View.GONE);
                                                            btnCon.setText(R.string.sign_in_login_button);
                                                            logInOrCreate = CONNEXION;
                                                            navigationMode.setText(R.string.create_new_account_login);
                                                            fireBaseAuth.signOut();
                                                        }
                                                        else {
                                                            fireBaseAuth.signOut();
                                                            userCreated.delete();
                                                            new BigPenToast(getContext(), R.drawable.ic_error, R.string.failed_account_create);
                                                        }
                                                    }
                                                });
                                    }
                                    else{
                                        bigPenProgressDialog.dismissMe();
                                        new BigPenToast(getContext(), R.drawable.ic_error, R.string.failed_account_create);
                                    }
                                }
                            });
                } else return;
                break;
        }

    }

    private boolean validateEmail(String email) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputEmail.setErrorEnabled(true);
            textInputEmail.setError(getString(R.string.message_email_login));
            return false;
        } else {
            textInputEmail.setErrorEnabled(false);
            textInputEmail.setError(null);
            return true;
        }
    }

    private boolean validateUserName(String userName) {
        if (userName.isEmpty() || userName.length() < 2) {
            textInputUserName.setErrorEnabled(true);
            textInputUserName.setError(getString(R.string.message_user_name_login));
            return false;
        } else {
            textInputUserName.setErrorEnabled(false);
            textInputUserName.setError(null);
            return true;
        }
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty() || password.length() < 6) {
            textInputPassword.setErrorEnabled(true);
            textInputPassword.setError(getString(R.string.message_password_login));
            return false;
        } else {
            textInputPassword.setErrorEnabled(false);
            textInputPassword.setError(null);
            return true;
        }
    }

    private void setErrorEnabled() {
        textInputEmail.setErrorEnabled(false);
        textInputEmail.setError(null);
        textInputEmail.getEditText().setText("");
        textInputUserName.setErrorEnabled(false);
        textInputUserName.setError(null);
        textInputUserName.getEditText().setText("");
        textInputPassword.setErrorEnabled(false);
        textInputPassword.setError(null);
        textInputPassword.getEditText().setText("");
    }
    private View.OnClickListener hideLookPWListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ImageButton imageButton = (ImageButton) view;
            if (hideMode) {
                textInputPassword.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                imageButton.setImageResource(R.drawable.ic_eye_unlook);
                hideMode = false;
            } else {
                textInputPassword.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD |
                        InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                imageButton.setImageResource(R.drawable.ic_eye_look);
                hideMode = true;
            }
            textInputPassword.getEditText().setSelection(textInputPassword.getEditText().getText().length());
        }
    };
    private View.OnClickListener navigationModeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setErrorEnabled();
            if (logInOrCreate.equals(CONNEXION)) {
                textInputUserName.setVisibility(View.VISIBLE);
                btnCon.setText(R.string.create_account_button);
                logInOrCreate = CREATE_ACCOUNT;
                navigationMode.setText(R.string.login_with_exist_account);
            }
            else {
                textInputUserName.setVisibility(View.GONE);
                btnCon.setText(R.string.sign_in_login_button);
                logInOrCreate = CONNEXION;
                navigationMode.setText(R.string.create_new_account_login);
            }
        }
    };
    private View.OnClickListener forgotPWListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setErrorEnabled();
            if (logInOrCreate.equals(RESET_PWD)) {
                btnCon.setText(R.string.sign_in_login_button);
                textInputEmail.requestFocus();
                textInputEmail.getEditText().setText(localPreferences.getUserCachedEmail());
                textInputEmail.getEditText().setSelection(textInputEmail.getEditText().getText().length());
                textInputUserName.setVisibility(View.GONE);
                passwordLayout.setVisibility(View.VISIBLE);
                navigationMode.setText(R.string.create_new_account_login);
                navigationMode.setVisibility(View.VISIBLE);
                resetPwd.setText(R.string.reset_pwd_link);
                logInOrCreate = CONNEXION;
            }
            else {
                btnCon.setText(R.string.ok_button);
                textInputEmail.requestFocus();
                logInOrCreate = RESET_PWD;
                textInputUserName.setVisibility(View.GONE);
                passwordLayout.setVisibility(View.GONE);
                navigationMode.setVisibility(View.GONE);
                resetPwd.setText(R.string.sign_in_login_button);
            }
        }
    };
    private View.OnClickListener btnConListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            loginOrCreate();
        }
    };
    private DrawerFragment getDrawerFragment() {
        return (DrawerFragment) getParentFragmentManager().findFragmentById(
                R.id.surfaceDrawFragment);
    }
    // tell MainActivityFragment that dialog is now displayed
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DrawerFragment fragment = getDrawerFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(true);
    }

    // tell MainActivityFragment that dialog is no longer displayed
    @Override
    public void onDetach() {
        super.onDetach();
        DrawerFragment fragment = getDrawerFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(false);
    }
}
