package io.github.recodex.android;

import android.os.Bundle;
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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.github.recodex.android.users.LoginHelper;

/**
 * Displays groups the user belongs to, along with some useful information
 */
public class CasLoginFragment extends Fragment {
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
        View view = inflater.inflate(R.layout.fragment_cas_login, container, false);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) view.findViewById(R.id.email_cas_login);

        mPasswordView = (EditText) view.findViewById(R.id.password_cas_login);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.password_cas_login_action || id == EditorInfo.IME_NULL) {
                    loginHelper.attemptCasLogin((LoginActivity) getActivity(), mEmailView, mPasswordView);
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) view.findViewById(R.id.sign_in_button_cas_login);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginHelper.attemptCasLogin((LoginActivity) getActivity(), mEmailView, mPasswordView);
            }
        });

        mLoginFormView = view.findViewById(R.id.cas_login_form);

        return view;
    }
}
