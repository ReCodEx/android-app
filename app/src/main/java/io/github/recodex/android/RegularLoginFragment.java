package io.github.recodex.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import io.github.recodex.android.helpers.LoginHelper;

/**
 * Displays groups the user belongs to, along with some useful information
 */
public class RegularLoginFragment extends Fragment {
    @Inject
    LoginHelper loginHelper;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApp) getContext().getApplicationContext()).getAppComponent().inject(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_regular_login, container, false);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) view.findViewById(R.id.email_regular_login);

        mPasswordView = (EditText) view.findViewById(R.id.password_regular_login);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    loginHelper.attemptLogin((LoginActivity) getActivity(), mEmailView, mPasswordView);
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) view.findViewById(R.id.sign_in_button_regular_login);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginHelper.attemptLogin((LoginActivity) getActivity(), mEmailView, mPasswordView);
            }
        });

        mLoginFormView = view.findViewById(R.id.regular_login_form);

        return view;
    }
}
