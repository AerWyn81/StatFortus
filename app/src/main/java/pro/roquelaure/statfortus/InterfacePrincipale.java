package pro.roquelaure.statfortus;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class InterfacePrincipale extends AppCompatActivity {

    private NumberPicker nPEffortAV, nPEffortAR;
    private TextView tv_warningOptAvancees, tv_portMachine, tv_motForceAV, tv_motForceAR, tv_motSecCumul, tv_motMinCumul, tv_motSecTmpTotal, tv_motMinTmpTotal, tv_poidsForceAVValue, tv_poidsForceARValue, tv_erreur, tv_erreur_info, tv_tempsTotalValue, tv_tempsEffortValue;
    private EditText ed_addrIpMachine, ed_portMachine, ed_motForceAV, ed_motForceAR, ed_motSecCumul, ed_motMinCumul, ed_motSecTmpTotal, ed_motMinTmpTotal;
    private Button btn;
    private InterfaceModbus interfaceModbus;
    private MaterialDialog mDialog;

    private String ipMachine = "";

    private int portMachine = 0;
    private int secCumul = 0;
    private int minCumul = 0;
    private int secTmpTotal = 0;
    private int minTmpTotal = 0;
    private int forceAv = 50;
    private int forceAr = 50;

    private int retry = 0;

    private boolean erreur = true;
    private boolean initViews = false;

    private SharedPreferences.Editor saveData;

    private Timer t,t2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //StrictMode is most commonly used to catch accidental disk or network access on the application's main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Affiche la vue XML du visuel de l'application
        setContentView(R.layout.interface_principale);
        //Instancie les vues de l'interface
        initViewsInterface();
        //Instancie les écouteurs d'évènement des numberPickers
        onValueChangeNumberPicker();

        new Task().execute(2); //2 -> Test Connexion
    }

    public void initViewsInterface() {
        //Initialisation des vues interface
        nPEffortAV = (NumberPicker) findViewById(R.id.nP_forceAV);
        nPEffortAR = (NumberPicker) findViewById(R.id.nP_forceAR);

        tv_poidsForceAVValue = (TextView) findViewById(R.id.tv_poidsForceAVValue);
        tv_poidsForceARValue = (TextView) findViewById(R.id.tv_poidsForceARValue);
        tv_erreur = (TextView) findViewById(R.id.tv_erreur);
        tv_erreur_info = (TextView) findViewById(R.id.tv_erreur_info);
        tv_tempsTotalValue = (TextView) findViewById(R.id.tv_tempsTotalValue);
        tv_tempsEffortValue = (TextView) findViewById(R.id.tv_tempsEffortValue);

        btn = (Button) findViewById(R.id.btnReset);
    }

    private void onValueChangeNumberPicker() {
        //Ecouteurs d'évènement des numberPickers
        nPEffortAV.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //Tâche de fond envoi de la force
                forceAv = newVal;
                tv_poidsForceAVValue.setText(getString(R.string.concat_value_forceAV,newVal));
            }
        });

        nPEffortAR.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                forceAr = newVal;
                tv_poidsForceARValue.setText(getString(R.string.concat_value_forceAR,newVal));
            }
        });
    }

    public void initViews() {
        //Instanciation des différentes vues dans leurs variables
        initViews = true;
        if (mDialog != null)
        {
            tv_warningOptAvancees = (TextView) mDialog.getCustomView().findViewById(R.id.tv_warningOptAvancees);
            tv_portMachine = (TextView) mDialog.getCustomView().findViewById(R.id.tv_portMachine);
            tv_motForceAV = (TextView) mDialog.getCustomView().findViewById(R.id.tv_motForceAV);
            tv_motForceAR = (TextView) mDialog.getCustomView().findViewById(R.id.tv_motForceAR);
            tv_motSecCumul = (TextView) mDialog.getCustomView().findViewById(R.id.tv_motSecCumul);
            tv_motMinCumul = (TextView) mDialog.getCustomView().findViewById(R.id.tv_motMinCumul);
            tv_motSecTmpTotal = (TextView) mDialog.getCustomView().findViewById(R.id.tv_motSecTmpTotal);
            tv_motMinTmpTotal = (TextView) mDialog.getCustomView().findViewById(R.id.tv_motMinTmpTotal);

            ed_addrIpMachine = (EditText) mDialog.getCustomView().findViewById(R.id.ed_addrIpMachine);
            ed_portMachine = (EditText) mDialog.getCustomView().findViewById(R.id.ed_portMachine);
            ed_motForceAV = (EditText) mDialog.getCustomView().findViewById(R.id.ed_motForceAV);
            ed_motForceAR = (EditText) mDialog.getCustomView().findViewById(R.id.ed_motForceAR);
            ed_motSecCumul = (EditText) mDialog.getCustomView().findViewById(R.id.ed_motSecCumul);
            ed_motMinCumul = (EditText) mDialog.getCustomView().findViewById(R.id.ed_motMinCumul);
            ed_motSecTmpTotal = (EditText) mDialog.getCustomView().findViewById(R.id.ed_motSecTmpTotal);
            ed_motMinTmpTotal = (EditText) mDialog.getCustomView().findViewById(R.id.ed_motMinTmpTotal);
        }
        else
        {
            LayoutInflater inflater = LayoutInflater.from(this);
            View theInflatedView = inflater.inflate(R.layout.dialog_config_apk, null);
            ed_addrIpMachine = (EditText) theInflatedView.findViewById(R.id.ed_addrIpMachine);
            ed_portMachine = (EditText) theInflatedView.findViewById(R.id.ed_portMachine);
            ed_motForceAV = (EditText) theInflatedView.findViewById(R.id.ed_motForceAV);
            ed_motForceAR = (EditText) theInflatedView.findViewById(R.id.ed_motForceAR);
            ed_motSecCumul = (EditText) theInflatedView.findViewById(R.id.ed_motSecCumul);
            ed_motMinCumul = (EditText) theInflatedView.findViewById(R.id.ed_motMinCumul);
            ed_motSecTmpTotal = (EditText) theInflatedView.findViewById(R.id.ed_motSecTmpTotal);
            ed_motMinTmpTotal = (EditText) theInflatedView.findViewById(R.id.ed_motMinTmpTotal);
        }
    }

    public void openSettings(View v) {
        //Même principe que toute à l'heure pour le dialog, celui là est dans l'apk, dont annulable si trompé en cliquant dessus
        erreur = true;
        mDialog = new MaterialDialog.Builder(this)
                .title(R.string.configurations_minimales)
                .customView(R.layout.dialog_config_apk, true)
                .positiveText(R.string.valider)
                .negativeText(R.string.annuler)

                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                })
                .show();

        initViews();
        View buttonValider = mDialog.getActionButton(DialogAction.POSITIVE);
        buttonValider.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!ed_addrIpMachine.getText().toString().equals("")) {
                    erreur = false;
                }
                if (!erreur) {
                    ipMachine = ed_addrIpMachine.getText().toString();
                    portMachine = Integer.parseInt(ed_portMachine.getText().toString());
                    t.cancel();
                    t.purge();
                    retry = 1;
                    new Task().execute(2); //2 -> Test Connexion
                } else {
                    ed_addrIpMachine.setError(getString(R.string.champ_incorrect_ou_vide));
                    Toast.makeText(InterfacePrincipale.this, R.string.erreur_renseigner_ip, Toast.LENGTH_LONG).show();
                }
            }
        });

        ed_addrIpMachine = (EditText) mDialog.getCustomView().findViewById(R.id.ed_addrIpMachine);
        ed_addrIpMachine.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")) {
                    if (s.toString().matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")) {
                        erreur = false;
                        ed_addrIpMachine.setError(null);
                    } else {
                        erreur = true;
                        ed_addrIpMachine.setError(getString(R.string.ip_incorrecte));
                    }
                } else {
                    erreur = true;
                    ed_addrIpMachine.setError(getString(R.string.champ_vide));
                }
            }
        });


        CheckBox cb_optAvancees = (CheckBox) mDialog.getCustomView().findViewById(R.id.cb_optAvancees);

        cb_optAvancees.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!initViews) initViews();
                    isVisibleOptAvancees(true);

                } else {
                    isVisibleOptAvancees(false);
                }
            }
        });
    }

    public void isVisibleOptAvancees(boolean state) {
        //Entrées visibles
        if (state) {
            tv_warningOptAvancees.setVisibility(View.VISIBLE);
            tv_portMachine.setVisibility(View.VISIBLE);
            tv_motForceAV.setVisibility(View.VISIBLE);
            tv_motForceAR.setVisibility(View.VISIBLE);
            tv_motSecCumul.setVisibility(View.VISIBLE);
            tv_motMinCumul.setVisibility(View.VISIBLE);
            tv_motSecTmpTotal.setVisibility(View.VISIBLE);
            tv_motMinTmpTotal.setVisibility(View.VISIBLE);

            ed_portMachine.setVisibility(View.VISIBLE);
            ed_motForceAV.setVisibility(View.VISIBLE);
            ed_motForceAR.setVisibility(View.VISIBLE);
            ed_motSecCumul.setVisibility(View.VISIBLE);
            ed_motMinCumul.setVisibility(View.VISIBLE);
            ed_motSecTmpTotal.setVisibility(View.VISIBLE);
            ed_motMinTmpTotal.setVisibility(View.VISIBLE);
        } else {
            //Entrées invisibles
            tv_warningOptAvancees.setVisibility(View.GONE);
            tv_portMachine.setVisibility(View.GONE);
            tv_motForceAV.setVisibility(View.GONE);
            tv_motForceAR.setVisibility(View.GONE);
            tv_motSecCumul.setVisibility(View.GONE);
            tv_motMinCumul.setVisibility(View.GONE);
            tv_motSecTmpTotal.setVisibility(View.GONE);
            tv_motMinTmpTotal.setVisibility(View.GONE);

            ed_portMachine.setVisibility(View.GONE);
            ed_motForceAV.setVisibility(View.GONE);
            ed_motForceAR.setVisibility(View.GONE);
            ed_motSecCumul.setVisibility(View.GONE);
            ed_motMinCumul.setVisibility(View.GONE);
            ed_motSecTmpTotal.setVisibility(View.GONE);
            ed_motMinTmpTotal.setVisibility(View.GONE);
        }
    }

    //Tâche de fond
    private class Task extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            //2 -> Test de connexion à la machine (méthode perso)
            if (params[0] == 2) {
                //Instanciation de la connexion avec une ip et un port temporaire (celui directement inscrit par l'utilisateur dans les editText)
                //interfaceModbus = new InterfaceModbus(ipMachineTmp, portMachineTmp);
                interfaceModbus = new InterfaceModbus(ipMachine, portMachine);

                return String.valueOf(interfaceModbus.testConnexion());
            }
            //3 -> Reset des temps
            else if (params[0] == 3) {
                //0 pour reset toutes les variables
                int[] val = {0,0,0,0};

                //int rr = interfaceModbus.setMultipleAddress(2,5,val);
                int a = interfaceModbus.setOneAddress(2,0);
                int b = interfaceModbus.setOneAddress(3,0);
                int c = interfaceModbus.setOneAddress(4,0);
                int rr = interfaceModbus.setOneAddress(5,0);
                if (rr == 0) {
                    //int rrr[] = interfaceModbus.getMultiAddress(2, 4);
                    int[] rrr = new int[4];
                    {
                        rrr[0] = interfaceModbus.getOneAddress(2);
                        rrr[1] = interfaceModbus.getOneAddress(3);
                        rrr[2] = interfaceModbus.getOneAddress(4);
                        rrr[3] = interfaceModbus.getOneAddress(5);
                    }
                    if (rrr.length > 1) {
                        return "resetTemps,"+Arrays.toString(rrr);
                    }
                    else {
                        return "1,";
                    }
                }
                else {
                    return "1,";
                }
            }
            //4 -> Défini les valeurs du premier démarrage à la machine
            else {
                int[] val2 = {50,50,0,0,0,0};
                int rr = 0;
                int a = interfaceModbus.setOneAddress(0,50);
                int b = interfaceModbus.setOneAddress(1,50);
                int c = interfaceModbus.setOneAddress(2,0);
                int d = interfaceModbus.setOneAddress(3,0);
                int e = interfaceModbus.setOneAddress(4,0);
                rr = interfaceModbus.setOneAddress(5,0);

                if (rr == 0) {
                    int[] rrr = new int[6];
                    {
                        rrr[0] = interfaceModbus.getOneAddress(0);
                        rrr[1] = interfaceModbus.getOneAddress(1);
                        rrr[2] = interfaceModbus.getOneAddress(2);
                        rrr[3] = interfaceModbus.getOneAddress(3);
                        rrr[4] = interfaceModbus.getOneAddress(4);
                        rrr[5] = interfaceModbus.getOneAddress(5);
                    }
                    if (rrr.length > 1) {
                        return "preload,"+Arrays.toString(rrr);
                    }
                    else {
                        return "1,";
                    }
                }
                else {
                    return "1,";
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            saveData = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
            //Remove le fichier
            saveData.remove(getString(R.string.app_name));
            saveData.clear();
            //Actualise le fichier
            saveData.apply();

            //Récupère le fichier de l'apk où je stocke les variables
            SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
            //Récupère les différentes info
            initViews();
            ipMachine = prefs.getString(getString(R.string.sharedPref_addrIpMachine), ed_addrIpMachine.getText().toString());
            portMachine = prefs.getInt(getString(R.string.sharedPref_portMachine), Integer.parseInt(ed_portMachine.getText().toString()));
            secCumul = prefs.getInt(getString(R.string.sharedPref_secCumul), Integer.parseInt(ed_motSecCumul.getText().toString()));
            minCumul = prefs.getInt(getString(R.string.sharedPref_minCumul), Integer.parseInt(ed_motMinCumul.getText().toString()));
            secTmpTotal = prefs.getInt(getString(R.string.sharedPref_motSecTmpTotal), Integer.parseInt(ed_motSecTmpTotal.getText().toString()));
            minTmpTotal = prefs.getInt(getString(R.string.sharedPref_motMinTmpTotal), Integer.parseInt(ed_motMinTmpTotal.getText().toString()));
            interfaceModbus = new InterfaceModbus(ipMachine, portMachine);
        }

        @Override
        protected void onPostExecute(String s) {
            String[] str = s.split("[,]");
            //Gestion de toutes les erreurs ou des retours des valeurs
            switch (str[0]) {
                case "0":
                    //Connexion OK donc liaison à un serveur modbus, méthode pour enregistrer les variables
                    saveAndStartProcessing();
                    if (mDialog != null)
                    {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                    retryCo(false);
                    break;

                case "1":
                    retryCo(true);
                    //Erreur
                    tv_erreur.setText(R.string.erreur);
                    //Pour ajouter une image à un textView programmaticalement
                    Drawable mDrawable = ContextCompat.getDrawable(InterfacePrincipale.this, R.drawable.ic_clear_black_24dp);
                    DrawableCompat.setTint(mDrawable, ContextCompat.getColor(InterfacePrincipale.this, R.color.colorAccent));
                    tv_erreur.setCompoundDrawablesWithIntrinsicBounds(mDrawable, null, null, null);
                    //Pour que tout soit aligné (image + texte)
                    tv_erreur.setGravity(Gravity.CENTER);
                    //Couleur du textView
                    tv_erreur.setTextColor(getColor(R.color.colorAccent));
                    //Erreur dans le textview qui se charge de l'affichage des erreurs à l'utilisateur
                    tv_erreur_info.setText(R.string.com_impossible);
                    //Couleur du textView
                    tv_erreur_info.setTextColor(getColor(R.color.colorAccent));
                    //Défini valeurs par défaut
                    nPEffortAV.setValue(0);
                    nPEffortAR.setValue(0);
                    tv_poidsForceAVValue.setText("--");
                    tv_poidsForceARValue.setText("--");
                    tv_tempsEffortValue.setText("--:--");
                    tv_tempsTotalValue.setText("--:--");
                    nPEffortAV.setEnabled(false);
                    nPEffortAR.setEnabled(false);
                    btn.setEnabled(false);
                    //Pour descendre la vue
                    final ScrollView mScrollView = (ScrollView)findViewById(R.id.sV);
                    mScrollView.post(new Runnable() {
                        public void run() {
                            mScrollView.scrollTo(0, mScrollView.getBottom());
                        }
                    });

                    if (retry == 1)
                    {
                        if (mDialog != null)
                        {
                            ed_addrIpMachine.setError(getString(R.string.erreur_ip));
                            retry = 0;
                        }
                    }
                    break;
                case "resetTemps":
                    if (str[1].trim().equals("0") && str[2].trim().equals("0")) {
                        tv_tempsEffortValue.setText("0" + str[1].trim() + ":" + "0" + str[2].trim());

                        if (str[3].trim().equals("0") && str[4].replace("]","").trim().equals("0")) {
                            tv_tempsTotalValue.setText("0" + str[3].trim()+ ":0" + str[4].replace("]","").trim());
                            getTimes();
                        } else {
                            //Erreur
                            tv_erreur.setText(R.string.erreur);
                            //Pour ajouter une image à un textView programmaticalement
                            Drawable mDrawable1 = ContextCompat.getDrawable(InterfacePrincipale.this, R.drawable.ic_clear_black_24dp);
                            DrawableCompat.setTint(mDrawable1, ContextCompat.getColor(InterfacePrincipale.this, R.color.colorAccent));
                            tv_erreur.setCompoundDrawablesWithIntrinsicBounds(mDrawable1, null, null, null);
                            //Pour que tout soit aligné (image + texte)
                            tv_erreur.setGravity(Gravity.CENTER);
                            //Couleur du textView
                            tv_erreur.setTextColor(getColor(R.color.colorAccent));

                            //Erreur dans le textview qui se charge de l'affichage des erreurs à l'utilisateur
                            tv_erreur_info.setText(R.string.reception_donnees);
                            //Couleur du textView
                            tv_erreur_info.setTextColor(getColor(R.color.colorAccent));
                        }
                    } else {
                        //Erreur
                        tv_erreur.setText(R.string.erreur);
                        //Pour ajouter une image à un textView programmaticalement
                        Drawable mDrawable2 = ContextCompat.getDrawable(InterfacePrincipale.this, R.drawable.ic_clear_black_24dp);
                        DrawableCompat.setTint(mDrawable2, ContextCompat.getColor(InterfacePrincipale.this, R.color.colorAccent));
                        tv_erreur.setCompoundDrawablesWithIntrinsicBounds(mDrawable2, null, null, null);
                        //Pour que tout soit aligné (image + texte)
                        tv_erreur.setGravity(Gravity.CENTER);
                        //Couleur du textView
                        tv_erreur.setTextColor(getColor(R.color.colorAccent));

                        //Erreur dans le textview qui se charge de l'affichage des erreurs à l'utilisateur
                        tv_erreur_info.setText(R.string.reception_donnees);
                        //Couleur du textView
                        tv_erreur_info.setTextColor(getColor(R.color.colorAccent));
                    }
                    break;
                case "preload":
                    //Reset tous les temps
                    tv_poidsForceAVValue.setText(getString(R.string.concat_value_forceAV,nPEffortAV.getValue()));
                    tv_poidsForceARValue.setText(getString(R.string.concat_value_forceAR,nPEffortAR.getValue()));

                    if (str[3].trim().equals("0") && str[4].trim().equals("0")) {
                        tv_tempsEffortValue.setText("0" + str[3].trim() + ":" + "0" + str[4].trim());

                        if (str[5].trim().equals("0") && str[6].replace("]","").trim().equals("0")) {
                            tv_tempsTotalValue.setText("0" + str[5].trim()+ ":0" + str[6].replace("]","").trim());
                        } else {
                            //Erreur
                            tv_erreur.setText(R.string.erreur);
                            //Pour ajouter une image à un textView programmaticalement
                            Drawable mDrawable1 = ContextCompat.getDrawable(InterfacePrincipale.this, R.drawable.ic_clear_black_24dp);
                            DrawableCompat.setTint(mDrawable1, ContextCompat.getColor(InterfacePrincipale.this, R.color.colorAccent));
                            tv_erreur.setCompoundDrawablesWithIntrinsicBounds(mDrawable1, null, null, null);
                            //Pour que tout soit aligné (image + texte)
                            tv_erreur.setGravity(Gravity.CENTER);
                            //Couleur du textView
                            tv_erreur.setTextColor(getColor(R.color.colorAccent));

                            //Erreur dans le textview qui se charge de l'affichage des erreurs à l'utilisateur
                            tv_erreur_info.setText(R.string.reception_donnees);
                            //Couleur du textView
                            tv_erreur_info.setTextColor(getColor(R.color.colorAccent));
                        }
                    } else {
                        //Erreur
                        tv_erreur.setText(R.string.erreur);
                        //Pour ajouter une image à un textView programmaticalement
                        Drawable mDrawable2 = ContextCompat.getDrawable(InterfacePrincipale.this, R.drawable.ic_clear_black_24dp);
                        DrawableCompat.setTint(mDrawable2, ContextCompat.getColor(InterfacePrincipale.this, R.color.colorAccent));
                        tv_erreur.setCompoundDrawablesWithIntrinsicBounds(mDrawable2, null, null, null);
                        //Pour que tout soit aligné (image + texte)
                        tv_erreur.setGravity(Gravity.CENTER);
                        //Couleur du textView
                        tv_erreur.setTextColor(getColor(R.color.colorAccent));

                        //Erreur dans le textview qui se charge de l'affichage des erreurs à l'utilisateur
                        tv_erreur_info.setText(R.string.reception_donnees);
                        //Couleur du textView
                        tv_erreur_info.setTextColor(getColor(R.color.colorAccent));
                    }
                    break;
            }
        }
    }

    private void saveAndStartProcessing() {
        //Appel de la méthode pour modifier le fichier de l'apk pour stocker les données dans l'appareil
        saveData = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
        //Affecte une clé pour une valeur
        initViews();
        saveData.putString(getString(R.string.sharedPref_addrIpMachine), ed_addrIpMachine.getText().toString());
        saveData.putInt(getString(R.string.sharedPref_portMachine), Integer.parseInt(ed_portMachine.getText().toString()));
        saveData.putInt(getString(R.string.sharedPref_ForceAv), Integer.parseInt(ed_motForceAV.getText().toString()));
        saveData.putInt(getString(R.string.sharedPref_ForceAr), Integer.parseInt(ed_motForceAR.getText().toString()));
        saveData.putInt(getString(R.string.sharedPref_secCumul), Integer.parseInt(ed_motSecCumul.getText().toString()));
        saveData.putInt(getString(R.string.sharedPref_minCumul), Integer.parseInt(ed_motMinCumul.getText().toString()));
        saveData.putInt(getString(R.string.sharedPref_motSecTmpTotal), Integer.parseInt(ed_motSecTmpTotal.getText().toString()));
        saveData.putInt(getString(R.string.sharedPref_motMinTmpTotal), Integer.parseInt(ed_motMinTmpTotal.getText().toString()));

        //Actualise et sauvegarde le fichier
        saveData.apply();
        //Pas d'erreur
        tv_erreur.setText(R.string.pas_d_erreur);
        //Pour ajouter une image à un textView programmaticalement
        Drawable mDrawable = ContextCompat.getDrawable(InterfacePrincipale.this, R.drawable.ic_check_black_24dp);
        DrawableCompat.setTint(mDrawable, ContextCompat.getColor(InterfacePrincipale.this, R.color.green));
        tv_erreur.setCompoundDrawablesWithIntrinsicBounds(mDrawable, null, null, null);
        //Pour que tout soit aligné (image + texte)
        tv_erreur.setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Couleur du textView
            tv_erreur.setTextColor(getColor(R.color.green));
        }
        //Efface si des infos sont restés dans le textView erreur_info
        tv_erreur_info.setText("");

        if (t != null)
        {
            t.cancel();
            t.purge();
        }
        if (t2 != null)
        {
            t2.cancel();
            t2.purge();
        }

        //Défini toutes les valeurs dans tous les mots de la machine
        new Task().execute(4); //4 -> Load toutes les configs + reset temps
        //Démarre le timer qui envoi toutes les 100ms une requête pour lire les registres des temps
        getTimes();

        nPEffortAV.setEnabled(true);
        nPEffortAR.setEnabled(true);
        btn.setEnabled(true);
        nPEffortAV.setValue(50);
        nPEffortAR.setValue(50);
    }

    public void getTimes() {
        //Instanciation du timer
        t = new Timer();
        //Boucle du timer toutes les 100 ms et exécute en tâche de fond la récupération des temps utilisateur
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                new Task2().execute();
            }
        };
        t.schedule(timerTask, 0, 100);
    }

    public void retryCo(boolean state)
    {
        if (state){
            t2 = new Timer();

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    new Task().execute(2);
                }
            };
            t2.schedule(timerTask,500);
        }
        else
        {
            if (t2 != null)
            {
                t2.cancel();
                t2.purge();
            }
        }
    }

    private class Task2 extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {
            if (interfaceModbus.setOneAddress(0,forceAv) == 0)
            {
                int r = interfaceModbus.setOneAddress(1,forceAr);
                //Récupération des temps
                int sec = interfaceModbus.getOneAddress(4);
                int min = interfaceModbus.getOneAddress(5);
                int secTotal = interfaceModbus.getOneAddress(2);
                int minTotal = interfaceModbus.getOneAddress(3);

                //Pour un meilleur affichage, je récupère juste "0" pour une valeur de seconde ou minute inférieur à 10, donc j'ajoute juste un "0" pour avoir "00", "01", "02"..
                String muchSec;
                if (sec < 10) {
                    muchSec = "0" + String.valueOf(sec);
                } else {
                    if (sec > 100) {
                        return "1";
                    }
                    else {
                        muchSec = String.valueOf(sec);
                    }
                }
                String muchMin;
                if (min < 10) {
                    muchMin = "0" + String.valueOf(min);
                } else {
                    if (min > 100) {
                        return "1";
                    }
                    else {
                        muchMin = String.valueOf(min);
                    }
                }
                String muchSec2;
                if (secTotal < 10) {
                    muchSec2 = "0" + String.valueOf(secTotal);
                } else {
                    if (secTotal > 100) {
                        return "1";
                    }
                    else {
                        muchSec2 = String.valueOf(secTotal);
                    }
                }
                String muchMin2;
                if (minTotal < 10) {
                    muchMin2 = "0" + String.valueOf(minTotal);
                } else {
                    if (minTotal > 100) {
                        return "1";
                    }
                    else {
                        muchMin2 = String.valueOf(minTotal);
                    }
                }
                //Retourne le temps
                return muchSec + "," + muchMin + "," + muchSec2 + "," + muchMin2;
            } else
            {
                return "1";
            }

        }

        @Override
        protected void onPostExecute(final String s) {
            //Si erreur
            if (s.equals("1")) {
                //Erreur
                tv_erreur.setText(R.string.erreur);
                //Pour ajouter une image à un textView programmaticalement
                Drawable mDrawable = ContextCompat.getDrawable(InterfacePrincipale.this, R.drawable.ic_clear_black_24dp);
                DrawableCompat.setTint(mDrawable, ContextCompat.getColor(InterfacePrincipale.this, R.color.colorAccent));
                tv_erreur.setCompoundDrawablesWithIntrinsicBounds(mDrawable, null, null, null);
                //Pour que tout soit aligné (image + texte)
                tv_erreur.setGravity(Gravity.CENTER);
                //Couleur du textView
                tv_erreur.setTextColor(getColor(R.color.colorAccent));

                //Erreur dans le textview qui se charge de l'affichage des erreurs à l'utilisateur
                tv_erreur_info.setText(R.string.probleme_envoi_donnees);
                //Couleur du textView
                tv_erreur_info.setTextColor(getColor(R.color.colorAccent));

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Pas d'erreur
                        tv_erreur.setText(R.string.pas_d_erreur);
                        //Pour ajouter une image à un textView programmaticalement
                        Drawable mDrawable2 = ContextCompat.getDrawable(InterfacePrincipale.this, R.drawable.ic_check_black_24dp);
                        DrawableCompat.setTint(mDrawable2, ContextCompat.getColor(InterfacePrincipale.this, R.color.green));
                        tv_erreur.setCompoundDrawablesWithIntrinsicBounds(mDrawable2, null, null, null);
                        //Pour que tout soit aligné (image + texte)
                        tv_erreur.setGravity(Gravity.CENTER);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //Couleur du textView
                            tv_erreur.setTextColor(getColor(R.color.green));
                        }
                        //Efface si des infos sont restés dans le textView erreur_info
                        tv_erreur_info.setText("");

                        //Affichage des temps en retour du traitement
                        String[] r = s.split("[,]");
                        tv_tempsEffortValue.setText(r[0] + ":" + r[1]);
                        tv_tempsTotalValue.setText(r[3] + ":" + r[2]);
                    }
                });
            }
        }
    }

    public void reinitTmpTotal(View v) {
        //Tâche de fond pour reset les temps
        t.cancel();
        t.purge();
        if (t2 != null)
        {
            t2.cancel();
            t2.purge();
        }
        Task task = new Task();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,3);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Si timer
        if (t != null)
        {
            //Arrête de le timer
            t.cancel();
            t.purge();
        }
        if (t2 != null)
        {
            //Arrête de le timer
            retryCo(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Si un timer était présent dans le onPause, on le restart
        if (t != null) {
            getTimes();
        }
        if (t2 != null) {
            retryCo(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Si données sauvegardées
        if (saveData != null)
        {
            //Récupère le fichier
            saveData = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
            //Remove le fichier
            saveData.remove(getString(R.string.app_name));
            saveData.clear();
            //Actualise le fichier
            saveData.apply();
        }
        //Si timer
        if (t != null)
        {
            //Arrête le timer
            t.cancel();
            t.purge();
        }
        if (t2 != null)
        {
            //Arrête le timer
            retryCo(false);
        }
    }
}
