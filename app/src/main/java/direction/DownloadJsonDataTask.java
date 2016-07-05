package direction;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import direction.DirectionsJSONParserTask.OnJSONParserCompleteListener;

/**
 * Created by Ran on 12/05/2016.
 */
public class DownloadJsonDataTask extends AsyncTask<String, Void, String> {

    private OnJSONParserCompleteListener mParserCompleteListener;

    public DownloadJsonDataTask(OnJSONParserCompleteListener listener){
        mParserCompleteListener = listener;
    }

    // Downloading data in non-ui thread
    @Override
    protected String doInBackground(String... url) {

        // For storing data from web service
        String data = null;

        try{
            // Fetching the data from web service
            data = downloadUrl(url[0]);
        }catch(Exception e){
            Log.d("Background Task",e.toString());
        }
        return data;
    }

    // Executes in UI thread, after the execution of
    // doInBackground()
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        DirectionsJSONParserTask directionsJSONParserTask = new DirectionsJSONParserTask();
        directionsJSONParserTask.addParserCompleteListener(mParserCompleteListener);

        // Invokes the thread for parsing the JSON data
        directionsJSONParserTask.execute(result);
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = null;
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("ExceptionDownloading", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

}
