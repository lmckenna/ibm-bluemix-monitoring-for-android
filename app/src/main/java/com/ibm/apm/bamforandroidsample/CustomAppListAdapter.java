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

    private final Activity context;

    private final ArrayList<String> appname;
    private final ArrayList<Integer> appimgid;
    private final ArrayList<Integer> stateimgid;
    private final ArrayList<String> statetext;
    private final ArrayList<String> appdesc;
    private final ArrayList<String> spaceguids;
    private final ArrayList<String> appguids;
    private final AppsList appslistthis;

    public CustomAppListAdapter(Activity context, ArrayList appname, ArrayList appimgid,
                                ArrayList stateimgid, ArrayList statetext,
                                ArrayList appdescriptions, ArrayList spaceguids,
                                ArrayList appguids, AppsList appslist) {
        super(context, R.layout.app_list_custom_card, appname);

        this.context = context;
        this.appname = appname;
        this.appimgid = appimgid;
        this.stateimgid = stateimgid;
        this.statetext = statetext;
        this.appdesc = appdescriptions;
        this.spaceguids = spaceguids;
        this.appguids = appguids;
        this.appslistthis = appslist;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.app_list_custom_card, null, true);

        TextView appName = (TextView) rowView.findViewById(R.id.Appname);
        ImageView appImageView = (ImageView) rowView.findViewById(R.id.icon);
        ImageView stateImageView = (ImageView) rowView.findViewById(R.id.stateIcon);
        TextView stateText = (TextView) rowView.findViewById(R.id.stateText);
        TextView descText = (TextView) rowView.findViewById(R.id.Appdesc);

        descText.setText(appdesc.get(position));
        appName.setText(appname.get(position));
        appImageView.setImageResource(appimgid.get(position));
        stateImageView.setImageResource(stateimgid.get(position));
        stateText.setText(statetext.get(position));

        Button onClickMonitoringButton = (Button) rowView.findViewById(R.id.monitoringResultsButton);
        onClickMonitoringButton.setTag(position);
        onClickMonitoringButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    Integer position=(Integer) v.getTag();
                    String selectedItem = appname.get(+position).toString();
                    String spaceguid = spaceguids.get(+position).toString();
                    String appguid = appguids.get(+position).toString();
                    //Toast.makeText(getApplicationContext(), selectedItem, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(appslistthis, TestResultsForAnApp.class);
                    intent.putExtra("app_name", selectedItem);
                    intent.putExtra("space_guid", spaceguid);
                    intent.putExtra("app_guid", appguid);
                    appslistthis.startActivity(intent);
            }
        });

        return rowView;
    }
}