package pilloni.lucien.consommationdualsim;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import pilloni.lucien.consommationdualsim.colorpicker.ColorPickerActivity;

/**
 * The configuration screen for the {@link ConsoDualSIM ConsoDualSIM} AppWidget.
 */
public class ConsoDualSIMConfigureActivity extends Activity {

	public static  ConsoDualSIMConfigureActivity _instance;

	public static final String PREFS_NAME = "ConsoDualSim";
	public static final String PREF_NOM = "NOM";
	public static final String PREF_NB_MINUTES = "NB MINUTES";
	public static final String PREF_DEBUT_ABO = "DEBUT ABO";
	public static final String PREF_COULEUR = "COULEUR";
	public static final String PREF_TRANSPARENCE = "TRANSPARENCE";
	public static final String PREF_ARRONDI = "ARRONDI";
	public static final String PREF_TEXTE = "TEXTE";
	public static final String PREF_SUBID = "SUBID";
	public static final int TEXTE_GAUCHE = 0;
	public static final int TEXTE_CENTRE = 1;
	public static final int TEXTE_DROITE = 2;
	public static final int TEXTE_AUTO = 3;
	//private static final String TAG = "DualSIMConfigure";

	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	public static final String COULEUR_SIM1 = "couleurSim1"; //$NON-NLS-1$
	public static final String COULEUR_SIM2 = "couleurSim2"; //$NON-NLS-1$
	int couleurSIM[] = new int[2];
	int _alignement;

	View.OnClickListener mOnClickListener = new View.OnClickListener() {
		public void onClick(View v)
		{
			final Context context = ConsoDualSIMConfigureActivity.this;
			SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor prefEditor = pref.edit();

			// When the button is clicked, store the string locally
			saveConfig(prefEditor, 1, R.id.editTextNomSIM, R.id.editTextNbMinutesSIM1, R.id.spinnerDebutSIM1, R.id.editTextSubscriptionID1);
			saveConfig(prefEditor, 2, R.id.editTextNomSIM2, R.id.editTextNbMinutesSIM2, R.id.spinnerDebutSIM2, R.id.editTextSubscriptionID2);

			prefEditor.putInt(PREF_TRANSPARENCE, ((SeekBar) findViewById(R.id.seekBarTransparence)).getProgress());
			prefEditor.putInt(PREF_ARRONDI, ((SeekBar) findViewById(R.id.seekBarArrondi)).getProgress());
			//prefEditor.putInt( PREF_TEXTE, ((Spinner)findViewById(R.id.spinnerTexte)).getSelectedItemPosition());
			prefEditor.putInt(PREF_TEXTE, _alignement);
			prefEditor.commit();

			// It is the responsibility of the configuration activity to update the app widget
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ConsoDualSIM.updateAppWidget(context, appWidgetManager, mAppWidgetId);

			// Make sure we pass back the original appWidgetId
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			setResult(RESULT_OK, resultValue);

			finish();
		}
	};

	public ConsoDualSIMConfigureActivity()
	{
		super();
	}

	/**
	 * Callback for the result from requesting permissions. This method
	 * is invoked for every call on {@link #requestPermissions(String[], int)}.
	 * <p>
	 * <strong>Note:</strong> It is possible that the permissions request interaction
	 * with the user is interrupted. In this case you will receive empty permissions
	 * and results arrays which should be treated as a cancellation.
	 * </p>
	 *
	 * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
	 * @param permissions  The requested permissions. Never null.
	 * @param grantResults The grant results for the corresponding permissions
	 *                     which is either {@link PackageManager#PERMISSION_GRANTED}
	 *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
	 * @see #requestPermissions(String[], int)
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		ConsoDualSIM.updateAppWidget(this, appWidgetManager, mAppWidgetId);
	}

	@Override
	public void onCreate(Bundle icicle)
	{
		_instance = this;
		super.onCreate(icicle);

		// Set the result to CANCELED.  This will cause the widget host to cancel
		// out of the widget placement if the user presses the back button.
		setResult(RESULT_CANCELED);

		setContentView(R.layout.conso_dual_sim_configure);
		//mAppWidgetText = (EditText) findViewById(R.id.appwidget_text);
		findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

		// Find the widget id from the intent.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null)
		{
			mAppWidgetId = extras.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If this activity was started with an intent without an app widget ID, finish with an error.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
		{
			finish();
			return;
		}

		loadConfig(1, R.id.editTextNomSIM, R.id.editTextNbMinutesSIM1, R.id.spinnerDebutSIM1, R.id.buttonCouleurSIM1, R.id.editTextSubscriptionID1);
		loadConfig(2, R.id.editTextNomSIM2, R.id.editTextNbMinutesSIM2, R.id.spinnerDebutSIM2, R.id.buttonCouleurSIM2,  R.id.editTextSubscriptionID2);

		SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);

		// Transparence du fond
		SeekBar seekbar = (SeekBar) findViewById(R.id.seekBarTransparence);
		seekbar.setMax(99);
		seekbar.setProgress(pref.getInt(PREF_TRANSPARENCE, 20));

		// Ratio de l'arrondi
		seekbar = (SeekBar) findViewById(R.id.seekBarArrondi);
		seekbar.setMax(99);
		seekbar.setProgress(pref.getInt(PREF_ARRONDI, 25));

		/*ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.alignement, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinner = (Spinner) findViewById(R.id.spinnerTexte);
		spinner.setAdapter(adapter);
		spinner.setSelection(pref.getInt(PREF_TEXTE, 0) );*/

		_alignement = pref.getInt(PREF_TEXTE, 0);
		checkToggleButton(_alignement);

		SIMS.getConsommation(this, 1, 25);
	}

	/***
	 * Configure les controles d'une des cartes SIM
	 * @param simNb
	 * @param editTextNomSIM
	 * @param editTextNbMinutesSIM
	 * @param spinnerDebutSIM
	 */
	private void loadConfig(int simNb, int editTextNomSIM, int editTextNbMinutesSIM, int spinnerDebutSIM, int button, int subId)
	{
		SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);

		TextView nom = (TextView) findViewById(editTextNomSIM);
		TextView nb = (TextView) findViewById(editTextNbMinutesSIM);
		TextView sub = (TextView) findViewById(subId);
		Spinner spinner = (Spinner) findViewById(spinnerDebutSIM);

		nom.setText(pref.getString(PREF_NOM + simNb, "sans nom " + simNb));
		nb.setText("" + pref.getInt(PREF_NB_MINUTES + simNb, 120));
		sub.setText("" + pref.getInt(PREF_SUBID + simNb, simNb));

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.jours, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(pref.getInt(PREF_DEBUT_ABO + simNb, 1) - 1);

		couleurSIM[simNb-1] = getResources().getColor(simNb == 1 ? R.color.SIM1 : R.color.SIM2);
		couleurSIM[simNb-1] = pref.getInt(PREF_COULEUR + simNb, couleurSIM[simNb-1]);
		findViewById(button).setBackgroundColor(couleurSansAlpha(couleurSIM[simNb-1]));
	}

	/***
	 * Retourne une couleur sans sa composante de transparence
	 * @param couleur
	 * @return
	 */
	private int couleurSansAlpha(int couleur)
	{
		return Color.argb(255, Color.red(couleur), Color.green(couleur), Color.blue(couleur));
	}

	/***
	 * Coche ou decoche le radio bouton correspondant a l'option de cadrage du texte
	 * @param toggledButton
	 */
	private void checkToggleButton(int toggledButton)
	{
		((ToggleButton) findViewById(R.id.toggleButtonDroite)).setChecked(toggledButton == TEXTE_DROITE);
		((ToggleButton) findViewById(R.id.toggleButtonCentre)).setChecked(toggledButton == TEXTE_CENTRE);
		((ToggleButton) findViewById(R.id.toggleButtonGauche)).setChecked(toggledButton == TEXTE_GAUCHE);
		((ToggleButton) findViewById(R.id.toggleButtonAuto)).setChecked(toggledButton == TEXTE_AUTO);
	}

	/***
	 * enregistre les controles d'une des cartes SIM
	 * @param simNb
	 * @param editTextNomSIM
	 * @param editTextNbMinutesSIM
	 * @param spinnerDebutSIM
	 */
	private void saveConfig(SharedPreferences.Editor prefEditor, int simNb, int editTextNomSIM, int editTextNbMinutesSIM, int spinnerDebutSIM, int editSubId)
	{

		TextView nom = (TextView) findViewById(editTextNomSIM);
		TextView nb = (TextView) findViewById(editTextNbMinutesSIM);
		TextView subid = (TextView) findViewById(editSubId);
		Spinner spinner = (Spinner) findViewById(spinnerDebutSIM);
		prefEditor.putString(PREF_NOM + simNb, nom.getText().toString());
		prefEditor.putInt(PREF_NB_MINUTES + simNb, Integer.valueOf(nb.getText().toString()));
		prefEditor.putInt(PREF_DEBUT_ABO + simNb, spinner.getSelectedItemPosition() + 1);
		prefEditor.putInt(PREF_COULEUR + simNb, couleurSIM[simNb-1]);
		prefEditor.putInt(PREF_SUBID + simNb, Integer.valueOf(subid.getText().toString()));

	}

	/***
	 * Clic sur le bouton pour changer la couleur de la premiere SIM
	 * @param v
	 */
	public void onClickCouleurSIM1(View v)
	{
		Intent it = new Intent(this, ColorPickerActivity.class);
		it.putExtra(ColorPickerActivity.TYPE_COULEUR, COULEUR_SIM1);

		it.putExtra(ColorPickerActivity.COULEUR, couleurSIM[0]);

		startActivityForResult(it, ColorPickerActivity.RESULT_CODE_COULEUR);
	}

	/***
	 * Click sur le bouton pour changer la couleur de la deuxieme SIM
	 * @param v
	 */
	public void onClickCouleurSIM2(View v)
	{
		Intent it = new Intent(this, ColorPickerActivity.class);
		it.putExtra(ColorPickerActivity.TYPE_COULEUR, COULEUR_SIM2);
		it.putExtra(ColorPickerActivity.COULEUR, couleurSIM[1]);

		startActivityForResult(it, ColorPickerActivity.RESULT_CODE_COULEUR);
	}

	/***
	 * Resultat de l'activity "ColorPicker"
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case ColorPickerActivity.RESULT_CODE_COULEUR:
				if (resultCode == RESULT_OK)
					if (data != null)
					{
						String typeCouleur = data.getStringExtra(ColorPickerActivity.TYPE_COULEUR);
						if (COULEUR_SIM1.equals(typeCouleur))
						{
							couleurSIM[0] = data.getIntExtra(ColorPickerActivity.COULEUR, Color.RED);
							findViewById(R.id.buttonCouleurSIM1).setBackgroundColor(couleurSansAlpha(couleurSIM[0]));
						}
						else if (COULEUR_SIM2.equals(typeCouleur))
						{
							couleurSIM[1] = data.getIntExtra(ColorPickerActivity.COULEUR, Color.BLUE);
							findViewById(R.id.buttonCouleurSIM2).setBackgroundColor(couleurSansAlpha(couleurSIM[1]));
						}
					}
				break;

			default:
				break;
		}
	}

	public void onClickTextGauche(View v)
	{
		_alignement = TEXTE_GAUCHE;
		checkToggleButton(_alignement);
	}

	public void onClickTextCentre(View v)
	{
		_alignement = TEXTE_CENTRE;
		checkToggleButton(_alignement);
	}

	public void onClickTextDroite(View v)
	{
		_alignement = TEXTE_DROITE;
		checkToggleButton(_alignement);
	}

	public void onClickTextAuto(View v)
	{
		_alignement = TEXTE_AUTO;
		checkToggleButton(_alignement);
	}

	public void onClickPreferences(View v)
	{
		Intent i = new Intent(this, PreferencesActivity.class);
		startActivity(i);
	}

	public void onClickSIM1(View v)
	{
		((ToggleButton)findViewById(R.id.toggleButtonSIM1)).setChecked(true);
		((ToggleButton)findViewById(R.id.toggleButtonSIM2)).setChecked(false);

		ViewFlipper vf = (ViewFlipper)findViewById(R.id.viewFlipperSIMS);
		vf.setOutAnimation(this, R.anim.animation_out);
		vf.setInAnimation(this, R.anim.animation_in);

		vf.setDisplayedChild(0);

	}

	public void onClickSIM2(View v)
	{
		((ToggleButton)findViewById(R.id.toggleButtonSIM1)).setChecked(false);
		((ToggleButton)findViewById(R.id.toggleButtonSIM2)).setChecked(true);

		ViewFlipper vf = (ViewFlipper)findViewById(R.id.viewFlipperSIMS);
		vf.setOutAnimation(this, R.anim.animation_out);
		vf.setInAnimation(this, R.anim.animation_in);

		vf.setDisplayedChild(1);
	}
}

