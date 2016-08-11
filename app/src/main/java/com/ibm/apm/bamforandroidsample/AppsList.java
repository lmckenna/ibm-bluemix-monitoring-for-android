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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.cloudfoundry.client.lib.CloudFoundryException;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class AppsList extends AppCompatActivity {

    private AppsListTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_list);

        getAppsList();

    }

    private void getAppsList() {
        Toast tToast;
        if (mAuthTask != null) {
            return;
        }

        tToast = Toast.makeText(getApplicationContext(), R.string.getting_application_list, Toast.LENGTH_LONG);
        tToast.show();

        mAuthTask = new AppsListTask(this);
        mAuthTask.execute((Void) null);
    }

    public class AppsListTask extends AsyncTask<Void, Void, Boolean> {

        private ArrayList<CloudApplication> mListOfApps;
        private AppsList mActivity;
        ArrayList listOfAppNames = new ArrayList();
        ArrayList listOfStateImages = new ArrayList();
        ArrayList listOfIcons = new ArrayList();
        ArrayList listOfStateTexts = new ArrayList();
        ArrayList listOfSpaceGUIDs = new ArrayList();
        ArrayList listOfAppGUIDs = new ArrayList();
        ArrayList listOfAppDescriptions = new ArrayList();

        AppsListTask(AppsList activity) {
            mActivity = activity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mListOfApps =  ((CfcSession)getApplicationContext()).listApps();

                // list the apps in the view
                for (CloudApplication application : mListOfApps) {
                    //listOfApps.add(application.getName());
                    listOfAppNames.add(application.getName());
                    JSONObject entity;

                    try {
                        // so I can get the buildpack used
                        Field f = application.getClass().getDeclaredField("entity");
                        f.setAccessible(true);
                        entity = (JSONObject) f.get(application);

                        String buildpackstring = entity.getString(CloudApplication.EntityField.detected_buildpack.name());
                        listOfSpaceGUIDs.add(entity.getString(CloudApplication.EntityField.space_guid.name()));
                        listOfAppDescriptions.add(entity.getString(CloudApplication.EntityField.package_state.name()));

                        String[] routes_url = entity.getString(CloudApplication.EntityField.routes_url.name()).split("/");
                        if (routes_url.length >= 4) {
                            listOfAppGUIDs.add(routes_url[3]);
                        }
                        else {
                            listOfAppGUIDs.add("");
                        }

                        if (buildpackstring == "null") {
                            buildpackstring = entity.getString(CloudApplication.EntityField.buildpack.name());
                        }
                        if (application.getState() == CloudApplication.AppState.STARTED) {
                            listOfStateImages.add(R.drawable.playbutton);
                            listOfStateTexts.add(getString(R.string.status_running));

                            if (buildpackstring.indexOf("ode") != -1) {
                                listOfIcons.add(R.drawable.nodejs);
                            } else if (buildpackstring.indexOf("Liberty") != -1) {
                                listOfIcons.add(R.drawable.liberty);
                            } else if (buildpackstring.indexOf("tatic") != -1) {
                                listOfIcons.add(R.drawable.html);
                            } else {// generic
                                listOfIcons.add(R.drawable.genericbuildpack);
                            }
                        } else { // STOPPED
                            listOfStateImages.add(R.drawable.stopbutton);
                            listOfStateTexts.add(getString(R.string.state_stopped));

                            if (buildpackstring.indexOf("ode") != -1) {
                                listOfIcons.add(R.drawable.nodejsgrey);
                            } else if (buildpackstring.indexOf("Liberty") != -1) {
                                listOfIcons.add(R.drawable.libertygrey);
                            } else if (buildpackstring.indexOf("tatic") != -1) {
                                listOfIcons.add(R.drawable.html);
                            } else {// generic
                                listOfIcons.add(R.drawable.genericbuildpack);
                            }
                        }
                        Log.i("AppsListLM: %s%n", application.getName());
                        Log.i("AppsBPLM: %s%n", buildpackstring);

                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            } catch (CloudFoundryException e) {
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
                // just empty list of apps, say no apps
                listOfAppNames.add(getString(R.string.empty_apps_list));
            }
            //ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity,
            //        R.layout.app_list_custom, R.id.Appname, listOfAppNames);
            //listView.setAdapter(adapter);

            ListView listView;
            CustomAppListAdapter adapter = new CustomAppListAdapter(mActivity, listOfAppNames,
                    listOfIcons, listOfStateImages, listOfStateTexts, listOfAppDescriptions, listOfSpaceGUIDs, listOfAppGUIDs, AppsList.this );
            listView = (ListView)findViewById(R.id.appListView);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    String selectedItem = listOfAppNames.get(+position).toString();
                    String spaceguid = listOfSpaceGUIDs.get(+position).toString();
                    String appguid = listOfAppGUIDs.get(+position).toString();
                    //Toast.makeText(getApplicationContext(), selectedItem, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AppsList.this, TestResultsForAnApp.class);
                    intent.putExtra("app_name", selectedItem);
                    intent.putExtra("space_guid", spaceguid);
                    intent.putExtra("app_guid", appguid);
                    AppsList.this.startActivity(intent);

                }
            });
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

}
