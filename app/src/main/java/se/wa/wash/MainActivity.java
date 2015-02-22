package se.wa.wash;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.i("MAIN", "Launch settings here!");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_generate, container, false);
            return rootView;
        }
    }

    public void clearEditText(View view) {
        EditText editText = (EditText) findViewById(R.id.FieldURL);
        editText.setText("");
    }

    public void pasteURL(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String pasteData = "";
        if(!(clipboard.hasPrimaryClip())) {
            // Do what?
        } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))) {
            // clipboard data is not plain text
        } else {
            //MIMETYPE_TEXT_URILIST
           ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
           pasteData = item.getText().toString();
           EditText editText = (EditText) findViewById(R.id.FieldURL);
           editText.setText(pasteData);
        }
    }

    public void copyURL(View view) {
        try {
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            EditText shortURL = (EditText) findViewById(R.id.short_url);
            CharSequence shorty = shortURL.getText();
            ClipData clip = ClipData.newPlainText("short_url", shorty);
            cb.setPrimaryClip(clip);
        } catch (Exception e) {
            Log.e("COPY","exception",e);
        }
    }

    public Boolean generateURL(View view) {

        // Get settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String washURLString = sharedPref.getString(SettingsActivity.KEY_PREF_WASH_API_URL, "");
        String washToken = sharedPref.getString(SettingsActivity.KEY_PREF_WASH_API_TOKEN,"");
        URL washURL;

        DialogFragment newFragment = new SettingsDialog(this);

        // TODO: Fire dialog to go to settings if values missing
        if(washToken.isEmpty() || washURLString.isEmpty()) {
            newFragment.show(getFragmentManager(), "SettingsDialog");
            return Boolean.FALSE;
        }

        // TODO: Not very pretty try/catch
        try {
            washURL = new URL(washURLString);

            // TODO: Open separate thread for this process
            ProgressDialog progress = new ProgressDialog(this);

            progress.setTitle("Generating Short URL");
            progress.setMessage("Please wait..");
            progress.setProgressStyle(progress.STYLE_SPINNER);
            progress.setCancelable(true);
            progress.show();

            EditText editText = (EditText) findViewById(R.id.FieldURL);
            String url = editText.getText().toString();
            editText.setEnabled(false);

            // Here we go..
            String shorty = this.wash(washURL, washToken, url);

            EditText shortURL = (EditText) findViewById(R.id.short_url);
            shortURL.setText(shorty);

            editText.setEnabled(true);
            progress.dismiss();


            /*
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i("GEN","Begin Thread");
                        Thread.sleep(2000);


                    } catch (Exception e) {
                        Log.e("GEN","Failed.");
                        Log.e("GEN","exception",e);
                    }
                    progress.dismiss();
                }
            });
            */

            /*
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Thread.sleep(2000);

                        //progress.setMessage("Almost done..");
                        Log.i("GEN","Almost done...");

                        Thread.sleep()



                        //progress.setMessage("Done!");
                        Log.i("GEN","Done!");
                        //Thread.sleep(500);



                    } catch (Exception e) {
                        e.getLocalizedMessage();
                    }
                }
            }).start();
            */
            return Boolean.TRUE;
        } catch (Exception e) {
            // Launch error dialog
            newFragment.show(getFragmentManager(), "SettingsDialog");
            return Boolean.FALSE;
        }
    }

    public String wash(URL washURL, String washToken, String url) {

        String str = null;
        InputStream is = null;

        Log.i("WASH","Generating url!");

        try {

            HttpURLConnection conn = (HttpURLConnection) washURL.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            // TODO: Not so pretty
            String input = "{ \"token\": \""+washToken+"\",\"url\": \""+url+"\" }";
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();
            conn.connect();
            is = conn.getInputStream();

            StringBuffer sb = new StringBuffer();
            String result = null;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            str =  sb.toString();
        } catch(Exception e) {
            Log.e("WASH","Failed.");
            Log.e("WASH","exception",e);
        }
        return str;
    }

}
