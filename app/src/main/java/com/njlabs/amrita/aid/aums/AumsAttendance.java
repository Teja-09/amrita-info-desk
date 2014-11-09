package com.njlabs.amrita.aid.aums;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.njlabs.amrita.aid.R;
import com.njlabs.amrita.aid.aums.classes.CourseAttendanceData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class AumsAttendance extends ActionBarActivity {

    ProgressDialog dialog;
    ListView list;
    String responseString = null;
    ArrayList<CourseAttendanceData> attendanceData = new ArrayList<CourseAttendanceData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            responseString = extras.getString("response");
        }

        setContentView(R.layout.activity_aums_data);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#e91e63"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Parsing the received data ");
        DataParser(responseString);
    }

    public void DataParser(String html) {
        Document doc = Jsoup.parse(html);

        Element table = doc.select("table[width=75%] > tbody").first();
        Elements rows = table.select("tr:gt(0)");

        CourseAttendanceData.deleteAll(CourseAttendanceData.class);

        for(Element row : rows) {
            Elements dataHolders = row.select("td > span");

            CourseAttendanceData adata = new CourseAttendanceData();

            adata.setCourseCode(dataHolders.get(0).text());
            adata.setCourseTitle(dataHolders.get(1).text());
            adata.setTotal(dataHolders.get(5).text());
            adata.setAttended(dataHolders.get(6).text());
            adata.setPercentage(dataHolders.get(7).text());

            adata.save();
            attendanceData.add(adata);
        }

        setupList();

    }
    public void setupList() {
        list = (ListView) findViewById(R.id.list);
        list.setBackgroundColor(getResources().getColor(R.color.white));

        ArrayAdapter<CourseAttendanceData> dataAdapter = new ArrayAdapter<CourseAttendanceData>(getBaseContext(), R.layout.item_aums_attendance, attendanceData) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_aums_attendance, null);
                }
                CourseAttendanceData data = getItem(position);
                ((TextView)convertView.findViewById(R.id.course_title)).setText(data.courseTitle);
                ((TextView)convertView.findViewById(R.id.attendance_status)).setText(Html.fromHtml("You attended <b>"+data.attended+"</b> of <b>"+data.total+"</b> classes"));
                ((TextView)convertView.findViewById(R.id.percentage)).setText(Math.round(data.percentage)+"%");

                if(Math.round(data.percentage)>=85) {
                    convertView.findViewById(R.id.indicator).setBackgroundResource(R.drawable.circle_green);
                }
                else if(Math.round(data.percentage)>=80) {
                    convertView.findViewById(R.id.indicator).setBackgroundResource(R.drawable.circle_yellow);
                }
                else {
                    convertView.findViewById(R.id.indicator).setBackgroundResource(R.drawable.circle_red);
                }

                return convertView;
            }
        };
        list.setAdapter(dataAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        list.setVisibility(View.VISIBLE);
        dialog.dismiss();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fadein,R.anim.fadeout);
    }
}
