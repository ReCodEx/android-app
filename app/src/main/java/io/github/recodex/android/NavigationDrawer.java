package io.github.recodex.android;

import android.accounts.Account;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import io.github.recodex.android.api.Constants;
import io.github.recodex.android.authentication.ReCodExAuthenticator;
import io.github.recodex.android.utils.Utils;

public class NavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final int MANAGE_ACCOUNTS_REQUEST = 666;

    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // handle current user
        handleAccounts();
    }

    private void fillUserInfo() {
        View header = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);

        // load the right preferences
        String userId = Utils.getAccountManager().getUserData(Utils.getCurrentAccount(), ReCodExAuthenticator.KEY_USER_ID);
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences(getString(R.string.user_preferences_prefix) + userId, Context.MODE_PRIVATE);;

        TextView userName = (TextView) header.findViewById(R.id.userName);
        userName.setText(prefs.getString(Constants.userFullName, "John Doe"));

        String avatarUrl = prefs.getString(Constants.userAvatarUrl, "");
        if (!avatarUrl.isEmpty()) {
            new DownloadImageTask((ImageView) header.findViewById(R.id.userAvatar))
                    .execute(avatarUrl);
        }
    }

    private void handleAccounts() throws SecurityException {
        Account accounts[] = Utils.getAccountManager().getAccountsByType(ReCodExAuthenticator.ACCOUNT_TYPE);

        if (accounts.length == 0) {
            // we have to login new user
            addNewAccount();
        } else if (accounts.length == 1) {
            // we have only one user, this is it... use him/her
            Utils.setCurrentAccount(accounts[0]);
            fillUserInfo();
        } else {
            showAccountPicker(accounts);
        }
    }

    private void addNewAccount() {
        final AccountManagerFuture<Bundle> future =
                Utils.getAccountManager().addAccount(ReCodExAuthenticator.ACCOUNT_TYPE,
                        ReCodExAuthenticator.AUTH_TOKEN_TYPE, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    fillUserInfo();
                    showMessage("authenticated");

                } catch (Exception e) {
                    // if not authenticated, close the app
                    finish();
                }
            }
        }, null);
    }

    private void showAccountPicker(final Account accounts[]) {
        String names[] = new String[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            names[i] = accounts[i].name;
        }

        // Account picker
        mAlertDialog = new AlertDialog.Builder(this).setTitle(R.string.pick_account)
                .setAdapter(new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, names), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.setCurrentAccount(accounts[which]);
                fillUserInfo();
                showMessage("user chosen");
            }
        }).create();
        mAlertDialog.show();
    }

    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            RoundedBitmapDrawable dr =
                    RoundedBitmapDrawableFactory.create(getResources(), result);
            dr.setCornerRadius(Math.max(result.getWidth(), result.getHeight()) / 2.0f);
            bmImage.setImageDrawable(dr);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MANAGE_ACCOUNTS_REQUEST:
                handleAccounts();
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_navigation_drawer_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_manage_accounts) {
            Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
            //intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[]{ Constants.accountAuthority });
            startActivityForResult(intent, MANAGE_ACCOUNTS_REQUEST);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
