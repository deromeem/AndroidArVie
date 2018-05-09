package fr.lla_sio.androidarvie;

import android.app.Application;
import android.content.Context;

/**
 * Created by ED on 25/12/2017.
 */

public class MyAppli extends Application {

    /** Context de l'application */
    private static Context context;

    /**
     * Lancement de l'application
     */
    public void onCreate() {
        super.onCreate();
        MyAppli.context 		= getApplicationContext();
    }

    /**
     * Retourne le context de l'application
     * @return Context de l'application
     */
    public static Context getContext()
    {
        return MyAppli.context;
    }
}