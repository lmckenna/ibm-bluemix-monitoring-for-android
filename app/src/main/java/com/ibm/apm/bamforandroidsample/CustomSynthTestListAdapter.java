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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class CustomSynthTestListAdapter extends ArrayAdapter<String> {

    private final Activity mContext;

    ArrayList<SyntheticTest> mTestList;

    public CustomSynthTestListAdapter(Activity context, ArrayList testList, ArrayList testNames) {
        super(context, R.layout.synth_test_custom, testNames);

        mContext = context;
        mTestList = testList;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.synth_test_custom, null, true);

        TextView testName = (TextView) rowView.findViewById(R.id.SynthTestName);
        ImageView webOrAPI = (ImageView) rowView.findViewById(R.id.weborapi);
        ImageView statusImageView = (ImageView) rowView.findViewById(R.id.synthGoodOrFailed);
        TextView authorText = (TextView) rowView.findViewById(R.id.SynthTestAuthor);
        TextView testurlText = (TextView) rowView.findViewById(R.id.SynthTestURL);
        TextView responseTimeText = (TextView) rowView.findViewById(R.id.ResponseTimeValue);

        testName.setText(mTestList.get(position).mName);
        webOrAPI.setImageResource(mTestList.get(position).mWebOrAapiId);
        statusImageView.setImageResource(mTestList.get(position).mStatusImgId);
        authorText.setText(mTestList.get(position).mAuthorText);
        testurlText.setText(mTestList.get(position).mUrlText);
        NumberFormat formatter = new DecimalFormat("#0.000");
        responseTimeText.setText(formatter.format(mTestList.get(position).mResponseTime));

        return rowView;
    }
}