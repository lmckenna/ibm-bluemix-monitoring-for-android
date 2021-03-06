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

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.JsonReader;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TestResultsForAnApp extends AppCompatActivity {
    private TestResultsForAnApp.TestResultsTask mAuthTask = null;
    private String mSpaceGUID;
    private String mAppGUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        Bundle bundle = getIntent().getExtras();
        String appName = bundle.getString("app_name");
        mSpaceGUID = bundle.getString("space_guid");
        mAppGUID = bundle.getString("app_guid");

        TextView appNameTV = (TextView) findViewById(R.id.appDetailsAppName);
        appNameTV.setText(appName);

        getTestResults();

    }

    private void getTestResults() {
        if (mAuthTask != null) {
            return;
        }

        Toast toast;
        toast = Toast.makeText(getApplicationContext(), R.string.getting_test_results, Toast.LENGTH_LONG);
        toast.show();

        boolean cancel = false;
        View focusView = null;

        if (cancel) {
            // something went wrong
            focusView.requestFocus();
        } else {
            // background task to get test results
            mAuthTask = new TestResultsTask(this);
            mAuthTask.execute((Void) null);
        }
    }

    public class TestResultsTask extends AsyncTask<Void, Void, Boolean> {

        private TestResultsForAnApp mActivity;
        ArrayList<SyntheticTest> mTestsArray = new ArrayList<>();

        TestResultsTask(TestResultsForAnApp activity) {
            mActivity = activity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                // TODO: Evil code below, ignores all SSL errors, can never GA with this!!
                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType) {
                            }
                        }
                };

                // Install the trust manager that trusts everything
                try {
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                } catch (Exception e) {
                }

                try {

                    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }

                    });
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }


                List<TestDetails> ltestDetails;
                String urlString = getString(R.string.apiuri) + getString(R.string.synthetic_tests_path)
                        + "?applicationID=" + mAppGUID; // need to filter on applicationID
                URL url = new URL(urlString);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

                String userCredentials = getString(R.string.basicauthuser) + ":" + getString(R.string.basicauthpass);
                String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes("UTF-8"), Base64.NO_WRAP);

                urlConnection.setRequestProperty("Authorization", basicAuth);
                urlConnection.setRequestProperty("X-TenantID", mSpaceGUID);
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                urlConnection.setRequestProperty("Accept", "*/*");

                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    ltestDetails = readTestDetailsJSON(in);

                    for (TestDetails testDetails : ltestDetails) {

                        String testResultsUrlString = getString(R.string.apiuri) + getString(R.string.aar_url)
                                + "?limit=1" + "&filter_transactionID=" + testDetails.mTestID;
                        URL testResultsUrl = new URL(testResultsUrlString);
                        HttpsURLConnection testResultsUrlConnection = (HttpsURLConnection) testResultsUrl.openConnection();
                        testResultsUrlConnection.setRequestProperty("Authorization", basicAuth);
                        testResultsUrlConnection.setRequestProperty("X-TenantID", mSpaceGUID);
                        testResultsUrlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                        testResultsUrlConnection.setRequestProperty("Accept", "*/*");

                        try {
                            List<TestResults> ltestResults;
                            InputStream testResultsIn = new BufferedInputStream(testResultsUrlConnection.getInputStream());
                            ltestResults = readTestResultsJSON(testResultsIn);

                            for (TestResults testResults : ltestResults) {
                                SyntheticTest test = new SyntheticTest();

                                test.mName = testDetails.mTestName;
                                test.mAuthorText = testDetails.mAuthorName;
                                test.mResponseTime = testResults.mLastResponseTime;

                                test.mUrlText = testDetails.mTestURL;
                                if (testDetails.mSyntheticType.equals("SeleniumLoad")) {
                                    test.mWebOrAapiId = R.drawable.web;
                                } else {
                                    test.mWebOrAapiId = R.drawable.api;
                                }
                                if (testResults.mStatus.equals("Good")) {
                                    test.mStatusImgId = R.drawable.greentick;
                                } else {
                                    test.mStatusImgId = R.drawable.redcross;
                                }
                                mTestsArray.add(test);
                                break; // should only be 1 as set limit to 1
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            testResultsUrlConnection.disconnect();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    final String eStr = e.toString();

                    new Thread()
                    {
                        @Override
                        public void run()
                        {
                            Looper.prepare();

                            new AlertDialog.Builder(mActivity)
                                    .setTitle("Connection Error")
                                    .setMessage("Cannot connect to API proxy\r\n\r\nDetail: You probably haven't modified api_endpoints_required.xml" + "\r\n\r\n" + eStr.toString())
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();

                            Looper.loop();
                        }
                    }.start();


                } finally {
                    urlConnection.disconnect();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                //finish();
            } else {

            }
            ListView listView;

            ArrayList<String> testNamesArray = new ArrayList<>();
            for (SyntheticTest test : mTestsArray) {
                testNamesArray.add(test.mName);
            }

            CustomSynthTestListAdapter adapter = new CustomSynthTestListAdapter(mActivity, mTestsArray, testNamesArray);
            listView = (ListView) findViewById(R.id.synthTestListView);
            listView.setAdapter(adapter);

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    class TestDetails {
        public String mTestName;
        public String mTestID;
        public String mApplicationID;
        public String mSyntheticType;
        public String mAuthorName;
        public String mTestURL;

        TestDetails(String testName, String testID, String appID, String synthType, String author, String testurl) {
            mTestName = testName;
            mTestID = testID;
            mApplicationID = appID;
            mSyntheticType = synthType;
            mAuthorName = author;
            mTestURL = testurl;
        }
    }

    class TestResults {
        public Double mLastResponseTime;
        public String mStatus;

        TestResults(double lastResponseTime, String status) {
            mLastResponseTime = lastResponseTime;
            mStatus = status;
        }
    }

    // test details reading
    public List<TestDetails> readTestDetailsJSON(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readTestDetailsArray(reader);
        } finally {
            reader.close();
        }
    }

    public List<TestDetails> readTestDetailsArray(JsonReader reader) throws IOException {
        List<TestDetails> testDetails = new ArrayList<TestDetails>();

        reader.beginArray();
        while (reader.hasNext()) {
            testDetails.add(readTestDetails(reader));
        }
        reader.endArray();
        return testDetails;
    }

    public TestDetails readTestDetails(JsonReader reader) throws IOException {
        String testName = null;
        String testID = null;
        String applicationID = null;
        String syntheticType = null;
        String authorName = null;
        String testURL = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("testName")) {
                testName = reader.nextString();
            } else if (name.equals("testId")) {
                testID = reader.nextString();
            } else if (name.equals("syntheticType")) {
                syntheticType = reader.nextString();
            } else if (name.equals("applicationId")) {
                applicationID = reader.nextString();
            } else if (name.equals("changeBy")) {
                authorName = reader.nextString();
            } else if (name.equals("configuration")) {
                testURL = readTestURL(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new TestDetails(testName, testID, applicationID, syntheticType, authorName, testURL);
    }

    public String readTestURL(JsonReader reader) throws IOException {
        String testURL = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("url")) {
                testURL = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return testURL;
    }


    // test results reading (just the latest one)
    public List<TestResults> readTestResultsJSON(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readTestResultsArray(reader);
        } finally {
            reader.close();
        }
    }

    public List<TestResults> readTestResultsArray(JsonReader reader) throws IOException {
        List<TestResults> testResults = new ArrayList<TestResults>();

        reader.beginArray();
        while (reader.hasNext()) {
            testResults.add(readTestResults(reader));
            break; // TODO for some reason this throws an exception if you let it keep going, only have 1 though... I think
        }
        //reader.endArray(); // TODO for some reason this crashes, should investigate at some point
        return testResults;
    }

    public TestResults readTestResults(JsonReader reader) throws IOException {
        Double responseTime = new Double(-1);
        String status = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("metrics")) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String metricsName = reader.nextName();
                    if (metricsName.equals("responseTime")) {
                        responseTime = reader.nextDouble();
                    } else if (metricsName.equals("status")) {
                        status = reader.nextString();
                    } else {
                        reader.skipValue();
                    }
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new TestResults(responseTime, status);
    }

}
