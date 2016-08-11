/*
 * Copyright 2016 IBM Corp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.apm.bamforandroidsample;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.cloudfoundry.client.compat.OAuth2AccessToken;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudFoundryException;
import org.cloudfoundry.client.lib.domain.CloudApplication;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lmckenna on 12-Jul-2016.
 *
 * Use this class as a class to store the CloudFoundry Client details (login/etc)
 * Will also use this to get the OAuth2 Token to use with other services/APIs
 */
public class CfcSession extends Application {
    private CloudFoundryClient mCfc;
    private OAuth2AccessToken mOa2token;

    @Override
    public void onCreate() {
        //reinitialize variable
        super.onCreate();
    }

    // TODO: The login can check if the OAuth2AccessToken is null, and only prompt for a login
    // if it is null. If the login with the token fails it could fall back to asking for the
    // username and password
    // Also - the token is actually a member of the CloudFoundryClient, so no need to keep track
    // of our own copy of it really

    public boolean login(String user, String password, String URL) {
        CloudCredentials credentials = new CloudCredentials(user, password);
        mCfc = new CloudFoundryClient(credentials, getTargetURL(URL));

        try {
            mOa2token = mCfc.login();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.spref_oa2token),mOa2token.getValue());
            editor.putString(getString(R.string.spref_oa2apiurl),URL);
            editor.apply();

        } catch (CloudFoundryException e) {
            e.printStackTrace();
            Log.i("Login exception: %s", e.toString());
            return false;
        }
        return true;
    }

    public boolean login(OAuth2AccessToken oa2token, String URL) {
        CloudCredentials credentials = new CloudCredentials(oa2token);
        mCfc = new CloudFoundryClient(credentials, getTargetURL(URL));

        try {
            mOa2token = mCfc.login();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.spref_oa2token),mOa2token.getValue());
            editor.putString(getString(R.string.spref_oa2apiurl),URL);
            editor.apply();

        } catch (CloudFoundryException e) {
            e.printStackTrace();
            Log.i("Login exception: %s", e.toString());
            return false;
        }
        return true;
    }

    public void logout () {
        mCfc.logout();
    }

    public ArrayList<CloudApplication> listApps() throws CloudFoundryException {
        ArrayList listOfApps = new ArrayList();
        try {
            List<CloudApplication> myList = mCfc.getApplications();
            for (CloudApplication application : myList) {
                listOfApps.add(application);
            }
        } catch (CloudFoundryException e) {
            e.printStackTrace();
        }
        return listOfApps;
    }

    private static URL getTargetURL(String target) {
        try {
            return URI.create(target).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("The target URL is not valid: " + e.getMessage());
        }
    }
}