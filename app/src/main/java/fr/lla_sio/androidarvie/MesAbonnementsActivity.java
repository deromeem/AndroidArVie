package fr.lla_sio.androidarvie;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MesAbonnementsActivity extends ListActivity {

    // paramètres de connexion à transmettre par intent à l'activité suivante
    String userLogin = "";
    String userPwd = "";
    private static final String TAG_USER_LOGIN = "login";
    private static final String TAG_USER_PWD = "pwd";

    ArrayList<HashMap<String, String>> abonnementsList = new ArrayList<HashMap<String, String>>();

    // noms des noeuds JSON
    private static final String TAG_ABONNEMENTS = "abonnements";
    private static final String TAG_ID = "id";
    private static final String TAG_ABONNE = "abonne";
    private static final String TAG_SUIT = "suit";
    private static final String TAG_DEPUIS = "depuis";

    // tableau JSON des abonnements
    JSONArray abonnements = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mes_abonnements);

        // récupération de l'intent de la vue courante
        Intent i = getIntent();
        // obtention des paramètres de connexion à partir de l'intent
        userLogin = i.getStringExtra(TAG_USER_LOGIN);
        userPwd = i.getStringExtra(TAG_USER_PWD);

        // chargement des abonnements en fil d'exécution de fond (background thread)
        new LoadAbonnements().execute();

        // création de la ListView lv
        ListView lv = getListView();

        // événement de clic sur lv déclenchant l'affichage de la vue de détail
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // recherche de l'aid de l'élément sélectionné (ListItem) dans la liste
                String aid = ((TextView) view.findViewById(R.id.aid)).getText().toString();
                // DEBUG : affichage temporaire de aid
                // Toast.makeText(MesAbonnementsActivity.this, "Abonnements aid : "+aid, Toast.LENGTH_LONG).show();

                // création d'une nouvelle intention (intent)
                Intent in = new Intent(getApplicationContext(), AbonnementActivity.class);
                in.putExtra(TAG_USER_LOGIN, userLogin);
                in.putExtra(TAG_USER_PWD, userPwd);
                // envoi de l'aid à l'activité suivante (activity)
                in.putExtra(TAG_ID, aid);
                // lancement de la nouvelle activité (vue de détail) en attente d'une réponse
                startActivityForResult(in, 100);
            }
        });
    }

    // réponse de l'activité vue de détail
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if (resultCode == 100) {
        if (requestCode == 100) {
            // raffraichissement de l'écran
            Intent in = getIntent();
            finish();
            startActivity(in);
        }
    }

    /**
     * Tâche de fond pour charger les abonnements par une requête HTTP
     */
    class LoadAbonnements extends AsyncTask<String, String, JSONObject> {

        // url pour obtenir la liste des abonnements
        String apiUrl = "";

        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        // affiche une barre de progression avant d'activer la tâche de fond
        @Override
        protected void onPreExecute() {
            // super.onPreExecute();
            pDialog = new ProgressDialog(MesAbonnementsActivity.this);
            pDialog.setMessage("Attente de connexion...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            apiUrl = "http://" + getString(R.string.pref_default_api_url_loc) + "/index.php";
            // apiUrl = "http://" + getString(R.string.pref_default_api_url_dist) + "/index.php";

            // Toast.makeText(MesAbonnementsActivity.this, "URL de l'API : " + apiUrl, Toast.LENGTH_LONG).show();
        }

        // obtention en tâche de fond des abonnements au format JSON par une requête HTTP
        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("login", userLogin);
                params.put("pwd", userPwd);
                params.put("task", "abonnements");

                Log.d("request", "starting");

                // JSONObject json = jsonParser.makeHttpRequest(apiUrl, "GET", params);
                JSONObject json = jsonParser.makeHttpRequest(apiUrl, "POST", params);

                if (json != null) {
                    Log.d("JSON result", json.toString());
                    return json;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        // ferme la boite de dialogue à la terminaison de la tâche de fond
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String message = "";

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (json != null) {
                // Toast.makeText(MesAbonnementsActivity.this, json.toString(), Toast.LENGTH_LONG).show();  // TEST/DEBUG
                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                Log.d("Success!", message);
                // Liste des abonnements trouvées => obtention du tableau des abonnements
                try {
                    abonnements = json.getJSONArray(TAG_ABONNEMENTS);
                    // boucle sur toutes les offres
                    for (int i = 0; i < abonnements.length(); i++) {
                        JSONObject obj = abonnements.getJSONObject(i);

                        // enregistrement de chaque élément JSON dans une variable
                        String id = obj.getString(TAG_ID);
                        String abonne = obj.getString(TAG_ABONNE);
                        String suit = obj.getString(TAG_SUIT);
                        String depuis = obj.getString(TAG_DEPUIS);

                        // création d'un nouveau HashMap
                        HashMap<String, String> map = new HashMap<>();

                        // ajout de chaque variable (clé, valeur) dans le HashMap
                        map.put(TAG_ID, id);
                        map.put(TAG_ABONNE, abonne);
                        map.put(TAG_SUIT, suit);
                        map.put(TAG_DEPUIS, depuis);

                        // ajout du HashMap dans le tableau des abonnements
                        abonnementsList.add(map);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Log.d("Failure", message);
            }

            // mise à jour de l'interface utilisateur (UI) depuis le thread principal
            runOnUiThread(new Runnable() {
                public void run() {
                    // mise à jour de la ListView avec les données JSON mises dans le tableau abonnementsList
                    ListAdapter adapter;
                    adapter = new SimpleAdapter(
                            MesAbonnementsActivity.this, abonnementsList,
                            R.layout.list_abonnement_item, new String[]{TAG_ID, TAG_ABONNE, TAG_SUIT, TAG_DEPUIS},
                            new int[]{R.id.aid, R.id.abonne, R.id.suit, R.id.depuis});
                    setListAdapter(adapter);
                }
            });
        }
    }
}
