package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;

import android.util.Log;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;

import com.dynatrace.android.agent.DTXAction;
import com.dynatrace.android.agent.Dynatrace;
import com.dynatrace.android.agent.conf.DynatraceConfigurationBuilder;

import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView tvServerResponse;

    //A modifier: l'adresse de votre service Azure
    private static final String SERVER = "http://168.62.186.148/"; //"http://13.73.138.121/";

    //A modifier: votre nom d'utilisateur
    private static final String userId = "android.studio@android.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //---------------Start the OneAgent
        Dynatrace.startup(this, new DynatraceConfigurationBuilder("b763f69a-f01d-4b58-b914-18fcc40740ae", "https://bf89185syo.bf-sprint.dynatracelabs.com/mbeacon")
                //Here you can add configurations
                .buildConfiguration());
        //---------------

        tvServerResponse = findViewById(R.id.textView);
        Button contactServerButton = findViewById(R.id.btn_get);
        contactServerButton.setOnClickListener(onSendClickListener);

        Button catsButton = findViewById(R.id.btn_cats);
        catsButton.setOnClickListener(onCatsClickListener);

        Button dogsButton = findViewById(R.id.btn_dogs);
        dogsButton.setOnClickListener(onDogsClickListener);

    }

    //Bouton Get Current Score
    View.OnClickListener onSendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //---------------Début de la User action
            DTXAction clickAction = Dynatrace.enterAction("Click on GET CURRENT SCORE button");

            //Interoge le serveur et récupère le score actuel
            HttpGetRequest request = new HttpGetRequest();
            request.execute();

            //---------------Identification de l'utilisateur
            Dynatrace.identifyUser(userId);

            //---------------Fin de la User action
            clickAction.leaveAction();
        }
    };

    //Bouton CATS
    View.OnClickListener onCatsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //---------------Début de la User action
            DTXAction clickAction = Dynatrace.enterAction("Click on CATS button");

            /* En chantier
            HttpPostRequest request = new HttpPostRequest();
            request.execute();
            Log.i("CATS", "Click on Cats");
            */

            //---------------Identification de l'utilisateur
            Dynatrace.identifyUser(userId);

            //---------------Fin de la User action
            clickAction.leaveAction();
        }
    };

    //Bouton DOGS
    View.OnClickListener onDogsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //---------------Début de la User action
            DTXAction clickAction = Dynatrace.enterAction("Click on DOGS button");

            /* En chantier
            HttpPostRequest request = new HttpPostRequest();
            request.execute();
            Log.i("CATS", "Click on Dogs");
            */

            //---------------Identification de l'utilisateur
            Dynatrace.identifyUser(userId);

            //---------------Fin de la User action
            clickAction.leaveAction();
        }
    };

    //Méthode Get pour récupérer le score
    public class HttpGetRequest extends AsyncTask<Void, Void, String> {

        static final String REQUEST_METHOD = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected String doInBackground(Void... params){
            String result;
            String inputLine;

            try {
                // connect to the server
                URL myUrl = new URL(SERVER);
                HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.connect();

                // get the string from the input stream
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }
                reader.close();
                streamReader.close();
                String html = stringBuilder.toString();

                //Parse the html
                Document doc = Jsoup.parse(html);
                //Get content from div id="results"
                String score = doc.getElementById("results").text();
                result = score.toString();

            } catch(IOException e) {
                e.printStackTrace();
                result = "Error: Failed to connect to the server";
            }

            return result;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Log.i("RESULT", result);
            tvServerResponse.setText(result);

        }
    }

    //En chantier: Méthode Post pour mettre à jour le score
    public class HttpPostRequest extends AsyncTask<Void, Void, String> {

        static final String REQUEST_METHOD = "POST";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected String doInBackground(Void... params){
            String result;

            try {
                // connect to the server
                URL myUrl = new URL(SERVER);
                HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                //connection.setRequestProperty("vote","reset");
                connection.setDoOutput(true);
                connection.connect();

                result = "reset";

                // Create data variable for sent values to server
                String data = URLEncoder.encode("vote", "UTF-8")
                        + "=" + URLEncoder.encode("reset", "UTF-8");

                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write( data );
                wr.flush();
                Log.i("DATA", data);

            } catch(IOException e) {
                e.printStackTrace();
                result = "error";
            }

            return result;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_gallery) {
            // start user action
            DTXAction clickAction = Dynatrace.enterAction("Click on Gallery");
            // ...do some work here...
            Log.i("GALLERY", "Click on Gallery");
            Dynatrace.identifyUser(userId);
            // end the action after the search completed
            clickAction.leaveAction();
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
