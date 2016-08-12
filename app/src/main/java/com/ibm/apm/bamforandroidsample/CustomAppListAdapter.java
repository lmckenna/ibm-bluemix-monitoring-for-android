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

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAppListAdapter extends ArrayAdapter<String> {

    private final Activity mContext;

    private ArrayList<Application> mAppListArray = new ArrayList<>();
    private final AppsList mAppsListThis;

    public CustomAppListAdapter(Activity context, ArrayList<Application> appsListArray, AppsList appslist, ArrayList<String> appNamesArray) {
        super(context, R.layout.app_list_custom_card, appNamesArray);

        mContext = context;
        mAppListArray = appsListArray;
        mAppsListThis = appslist;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.app_list_custom_card, null, true);

        TextView appName = (TextView) rowView.findViewById(R.id.Appname);
        ImageView appImageView = (ImageView) rowView.findViewById(R.id.icon);
        ImageView stateImageView = (ImageView) rowView.findViewById(R.id.stateIcon);
        TextView stateText = (TextView) rowView.findViewById(R.id.stateText);
        TextView descText = (TextView) rowView.findViewById(R.id.Appdesc);

        if (!mAppListArray.isEmpty()) {
            descText.setText(mAppListArray.get(position).mDescription);
            appName.setText(mAppListArray.get(position).mName);
            appImageView.setImageResource(mAppListArray.get(position).mImgId);
            stateImageView.setImageResource(mAppListArray.get(position).mStateImgId);
            stateText.setText(mAppListArray.get(position).mStateText);

            Button onClickMonitoringButton = (Button) rowView.findViewById(R.id.monitoringResultsButton);
            onClickMonitoringButton.setTag(position);
            onClickMonitoringButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Integer position = (Integer) v.getTag();
                    String selectedItem = mAppListArray.get(+position).mName;
                    String spaceguid = mAppListArray.get(+position).mSpaceGuid;
                    String appguid = mAppListArray.get(+position).mAppGuid;
                    Intent intent = new Intent(mAppsListThis, TestResultsForAnApp.class);
                    intent.putExtra("app_name", selectedItem);
                    intent.putExtra("space_guid", spaceguid);
                    intent.putExtra("app_guid", appguid);
                    mAppsListThis.startActivity(intent);
                }
            });
        }

        return rowView;
    }
}