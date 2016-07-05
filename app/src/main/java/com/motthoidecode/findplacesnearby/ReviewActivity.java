package com.motthoidecode.findplacesnearby;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import utils.Util;

public class ReviewActivity extends AppCompatActivity {

    private int mPlaceId;
    private EditText etContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        findViewById(R.id.ratingBar).setEnabled(false);

        mPlaceId = getIntent().getIntExtra(Util.KEY_ID,-1);

        etContent = (EditText) findViewById(R.id.etContent);
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView tvContentLength = (TextView)findViewById(R.id.tvContentLength);
                tvContentLength.setText(s.length() + " ký tự");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void cancel(View v){
        this.finish();
    }

    public void report(View v){
        String content = etContent.getText().toString().trim();
        if(content.length() == 0){
            etContent.setError("");
            etContent.requestFocus();
            return;
        }
        final ProgressDialog dialog = new ProgressDialog(ReviewActivity.this);
        dialog.setTitle("Please wait...");
        dialog.setMessage("Saving review...");
        dialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.setConnectTimeout(1000 * 30);
        client.setResponseTimeout(1000 * 20);
        RequestParams params = new RequestParams();

        params.put("placeId", mPlaceId);
        params.put("content", content);

        client.post(Util.URL_ADD_REVIEW, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                if (dialog.isShowing()) dialog.dismiss();
                // TODO Auto-generated method stub
                try {
                    int success = response.getInt("success");
                    if (success == 1) {
                        showToastMessage("Saved");
                        if (dialog.isShowing()) dialog.dismiss();
                        ReviewActivity.this.setResult(MapsActivity.RESULT_CODE_ADD_REVIEW_OK);
                        ReviewActivity.this.finish();
                    } else {
                        String msg = response.getString("message");
                        showToastMessage(msg.substring(0, msg.length() > 50 ? 50 : msg.length()));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    String msg = e.toString();
                    showToastMessage(msg.substring(0, msg.length() > 50 ? 50 : msg.length()));
                    if (dialog.isShowing()) dialog.dismiss();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                Log.v("Err", errorResponse.toString());
                showToastMessage("Error!");

                if (dialog.isShowing()) dialog.dismiss();
            }
        });
    }

    private void showToastMessage(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }
}
