package network;

import android.os.AsyncTask;
import android.util.Log;

import com.motthoidecode.findplacesnearby.MapsActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Ran on 16/05/2016.
 */
public class DownloadJSONStringTask extends AsyncTask<String, Void, String> {

    private MapsActivity mActivity;
    private boolean mIsForReviewing = false;

    public DownloadJSONStringTask(MapsActivity mapsActivity) {
        this.mActivity = mapsActivity;
    }

    public DownloadJSONStringTask(MapsActivity mapsActivity, boolean isForReviewing) {
        this.mActivity = mapsActivity;
        mIsForReviewing = isForReviewing;
    }

    @Override
    protected String doInBackground(String... URL) {
        String jsonStr;
        try {
            Log.v("URL", URL[0]);
            jsonStr = getJsonString(URL[0]);
        } catch (Exception e) {
            jsonStr = null;
        } finally {

        }
        return jsonStr;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            if (mIsForReviewing)
                mActivity.onGetReviewComplete(result);
            else
                mActivity.onQueryPlacesComplete(result);
        } else{
                mActivity.showToastMessage("Null Json String");
        }

    }

    public String getJsonString(String strUrl) {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream in = null;

        try {
            url = new URL(strUrl);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

            in = conn.getInputStream();

            StringBuffer strBuffer = new StringBuffer();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strBuffer.append(line + "\\r\n");
            }
            in.close();
            return strBuffer.toString();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } finally {
            try {
                conn.disconnect();
            } catch (Exception e) {
            }

        }
    }
}
