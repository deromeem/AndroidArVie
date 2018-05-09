package fr.lla_sio.androidarvie;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // paramètres de connexion à transmettre par intent à l'activité suivante
    String userLogin = "";
    String userPwd = "";
    private static final String TAG_USER_LOGIN = "login";
    private static final String TAG_USER_PWD = "pwd";

    // contrôles de la vue (layout) correspondante
    EditText edtLogin, edtPwd;
    Button btnLogin;

    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();

    String userName = "";
    String userEmail = "";

    // noms des noeuds JSON
    private static final String TAG_USER = "user";
    private static final String TAG_USER_NAME = "name";
    private static final String TAG_USER_EMAIL = "email";

    AlertDialog.Builder alert;
    JSONObject json;
    int success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtLogin = (EditText) findViewById(R.id.edtLogin);
        edtPwd = (EditText) findViewById(R.id.edtPwd);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtLogin.getText().toString().equals("") || edtPwd.getText().toString().equals(""))
                {
                    Toast.makeText(MainActivity.this, "Vous devez remplir tous les champs", Toast.LENGTH_LONG).show();
                }
                else
                {
                    // lance la demande de connexion en fil d'exécution de fond (background thread)
                    userLogin = edtLogin.getText().toString();
                    userPwd = edtPwd.getText().toString();
                    new GoLogin().execute();
                    // GoLogin user= new GoLogin();
                    // user.execute();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //ajoute les entrées de menu_test à l'ActionBar
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //gère le click sur une action de l'ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // gère la sélection d'un élément du menu :
        switch (item.getItemId()){
//            case R.id.action_save:
//                save();
//                return true;
//            case R.id.action_delete:
//                delete();
//                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, MyPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class GoLogin extends AsyncTask<String, String, JSONObject> {

        // url de login
        String apiUrl = "";

        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        // affiche une barre de progression avant d'activer la tâche de fond
        @Override
        protected void onPreExecute() {
            // super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Attente de connexion...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            // apiUrl = "http://" + getString(R.string.pref_default_api_url_loc) + "/index.php";
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            apiUrl = "http://" + SP.getString("PREF_API_URL_LOC", getString(R.string.pref_default_api_url_loc)) + "/index.php";
            String prefAPI = SP.getString("PREF_API", "0");
            if (prefAPI.equals("1")) {
                apiUrl = "http://" + SP.getString("PREF_API_URL_DIST", getString(R.string.pref_default_api_url_dist)) + "/index.php";
            }
            // Toast.makeText(MainActivity.this,"URL de l'API : " + apiUrl,Toast.LENGTH_LONG).show();
        }

        // obtention en tâche de fond de la réponse au format JSON par une requête HTTP
        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("login", userLogin);
                params.put("pwd", userPwd);

                Log.d("request", "starting");

                // JSONObject json = jsonParser.makeHttpRequest(apiUrl, "GET", params);
                JSONObject json = jsonParser.makeHttpRequest(apiUrl, "POST", params);

                if (json != null) {
                    Log.d("JSON result", json.toString());
                    return json;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this,"Identifiant ou mot de passe incorrect !",Toast.LENGTH_LONG).show();
            }
            return null;
        }

        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String message = "";

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (json != null) {
                // Toast.makeText(MainActivity.this, json.toString(), Toast.LENGTH_LONG).show();
                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                Log.d("Success!", message);
                // Details du user bien reçus
                try {
                    // user = json.getJSONArray(TAG_USER);
                    // JSONObject u = user.getJSONObject(0);
                    JSONObject u = json.getJSONObject(TAG_USER);
                    // obtention du premier objet offre à partir du tableau JSON

                    // enregistrement de chaque élément JSON dans une variable
                    userName = u.getString(TAG_USER_NAME);
                    userEmail = u.getString(TAG_USER_EMAIL);
                    // String testEmail = userEmail;

                    // préparation de l'intent pour appel de MainScreenActivity (écran menu principal)
                    Intent in = new Intent(getApplicationContext(), MainScreenActivity.class);
                    in.putExtra(TAG_USER_NAME, userName);
                    in.putExtra(TAG_USER_LOGIN, userLogin);
                    in.putExtra(TAG_USER_PWD, userPwd);
                    // lancement de la nouvelle activité
                    startActivity(in);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Log.d("Failure", message);
                // login incorrect !
                alert = new AlertDialog.Builder(MainActivity.this);
                alert.setMessage("Identifiant ou mot de passe incorrect : réessayer !");
                alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        }
    }
}
