package pilloni.lucien.consommationdualsim;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ConsoDualSIMConfigureActivity ConsoDualSIMConfigureActivity}
 */
public class ConsoDualSIM extends AppWidgetProvider {
	static final String TAG = "ConsoDualSIM";
	static final String ACTION_CONFIGURE = "lpi.ConsoDualSim.CONFIGURE";
	static TelephonyManager tManager;
	private static int lastState = TelephonyManager.CALL_STATE_IDLE;
	private static boolean isIncoming;

	static int INSET_JAUGE, INSET_DATE, INSET_CONSO;
	static boolean JAUGE_DEGRADE, FOND_NOIR;

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
	                            int appWidgetId)
	{
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
		INSET_JAUGE = Integer.valueOf(SP.getString(context.getResources().getString(R.string.pref_inset_jauge), "2"));
		INSET_CONSO = Integer.valueOf(SP.getString(context.getResources().getString(R.string.pref_inset_jauge_consommation), "4"));
		INSET_DATE = Integer.valueOf(SP.getString(context.getResources().getString(R.string.pref_inset_jauge_date), "8"));
		JAUGE_DEGRADE = SP.getBoolean(context.getResources().getString(R.string.pref_jauges_degradees), true);
		FOND_NOIR = SP.getBoolean(context.getResources().getString(R.string.pref_fond_noir), false);
		// Construct the RemoteViews object
		Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
		final int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
		final int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
		final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.conso_dual_sim);

		SharedPreferences pref = context.getSharedPreferences(ConsoDualSIMConfigureActivity.PREFS_NAME, 0);
		int color = FOND_NOIR ?
				            Color.argb((pref.getInt(ConsoDualSIMConfigureActivity.PREF_TRANSPARENCE, 25) * 255 / 100), 0, 0, 0) :
				            Color.argb((pref.getInt(ConsoDualSIMConfigureActivity.PREF_TRANSPARENCE, 25) * 255 / 100), 255, 255, 255);
		rv.setInt(R.id.layout_main, "setBackgroundColor", color);
		RegisterOnClickListener(context, rv, appWidgetId);
		RegisterPhoneStateListener(context);

		if (minWidth > 0 && minHeight > 0)
		{
			rv.setImageViewBitmap(R.id.imageView, construitBitmap(context, pref, minWidth, minHeight));
			// Instruct the widget manager to update the widget
			appWidgetManager.updateAppWidget(appWidgetId, rv);
		}
	}

	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
	{
		// This is how you get your changes.
		/*int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
		int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
		int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
		int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
*/
		updateAppWidget(context, appWidgetManager, appWidgetId);
	}

	/***
	 * Construit la bitmap qui va representer les deux jauges
	 * @param largeur
	 * @param hauteur
	 * @return
	 */
	static private Bitmap construitBitmap(Context context, SharedPreferences pref, int largeur, int hauteur)
	{
		// Construire la bitmap
		Bitmap bm = Bitmap.createBitmap(largeur, hauteur, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bm);
		Paint paint = new Paint();
		paint.setColor(context.getResources().getColor(R.color.SIM1));
		paint.setAntiAlias(false);
		paint.setStyle(Paint.Style.FILL);

		if (largeur > hauteur)
		{
			Rect r1 = new Rect(0, 0, largeur, hauteur / 2);
			Rect r2 = new Rect(0, hauteur / 2, largeur, hauteur);
			dessineJauge(context, pref, canvas, 1, r1);
			dessineJauge(context, pref, canvas, 2, r2);
		}
		else
		{
			int C = Math.min(largeur, hauteur) / 2;
			Matrix m = new Matrix();
			m.setRotate(90, C, C);
			canvas.setMatrix(m);
			Rect r1 = new Rect(0, 0, hauteur, largeur / 2);
			Rect r2 = new Rect(0, largeur / 2, hauteur, largeur);
			dessineJauge(context, pref, canvas, 1, r1);
			dessineJauge(context, pref, canvas, 2, r2);
		}
		return bm;
	}

	static private void dessineJauge(Context context, SharedPreferences pref, Canvas canvas, int noSIM, Rect r1)
	{
		// Valeur de la jauge
		String nom = pref.getString(ConsoDualSIMConfigureActivity.PREF_NOM + noSIM, "sans nom " + noSIM);
		final int nbMinutesMax = pref.getInt(ConsoDualSIMConfigureActivity.PREF_NB_MINUTES + noSIM, 120);
		final int debutAbo = pref.getInt(ConsoDualSIMConfigureActivity.PREF_DEBUT_ABO + noSIM, 1);
		final int subscriptionID = pref.getInt(ConsoDualSIMConfigureActivity.PREF_SUBID + noSIM, 1);
		final int nbMinutesConsommees = SIMS.getConsommation(context, subscriptionID, debutAbo);
		final int couleur = pref.getInt(ConsoDualSIMConfigureActivity.PREF_COULEUR + noSIM, context.getResources().getColor(noSIM == 1 ? R.color.SIM1 : R.color.SIM2));
		final float ratioRayon = (pref.getInt(ConsoDualSIMConfigureActivity.PREF_ARRONDI, 25) / 200.0f);
		final int alignement = pref.getInt(ConsoDualSIMConfigureActivity.PREF_TEXTE, 0);

		// Trace le fond de la jauge
		{
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(couleur);
			RectF rJauge = new RectF(r1);
			rJauge.inset(INSET_JAUGE, INSET_JAUGE);
			float rayon = Math.min(rJauge.width(), rJauge.height()) * ratioRayon;

			canvas.drawRoundRect(rJauge, rayon, rayon, paint);
		}

		// Progression de la date
		{
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.FILL);

			RectF rJaugeDate = new RectF(r1);
			rJaugeDate.inset(INSET_DATE, INSET_DATE);
			float pourcent = calculProgressionDate(debutAbo);
			paint.setColor(Color.argb(64, 255, 255, 255));
			rJaugeDate.right = rJaugeDate.left + (int) (rJaugeDate.width() * pourcent);
			float rayon = Math.min(rJaugeDate.width(), rJaugeDate.height()) * ratioRayon;
			canvas.drawRoundRect(rJaugeDate, rayon, rayon, paint);
		}

		// Progression de la consommation
		float pourcent = ((float) nbMinutesConsommees / (float) nbMinutesMax);

		{
			RectF rJaugeConso = new RectF(r1);
			rJaugeConso.inset(INSET_CONSO, INSET_CONSO);

			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.FILL);
			int col = Color.argb(255, Color.red(couleur), Color.green(couleur), Color.blue(couleur));
			paint.setShadowLayer(2, 2, 2, Color.argb(100, 0, 0, 0));
			if (JAUGE_DEGRADE)
				paint.setShader(new LinearGradient(rJaugeConso.left, rJaugeConso.top, rJaugeConso.left, rJaugeConso.bottom, clair(col), fonce(col), Shader.TileMode.CLAMP));
			else
				paint.setColor(col);

			rJaugeConso.right = rJaugeConso.left + (int) (rJaugeConso.width() * pourcent);
			float rayon = Math.min(rJaugeConso.width(), rJaugeConso.height()) * ratioRayon;
			canvas.drawRoundRect(rJaugeConso, rayon, rayon, paint);
		}


		// Texte
		{
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.WHITE);
			paint.setShadowLayer(2, 2, 2, Color.argb(100, 0, 0, 0));
			switch (alignement)
			{
				case ConsoDualSIMConfigureActivity.TEXTE_DROITE:
					paint.setTextAlign(Paint.Align.RIGHT);
					break;

				case ConsoDualSIMConfigureActivity.TEXTE_CENTRE:
					paint.setTextAlign(Paint.Align.CENTER);
					break;

				case ConsoDualSIMConfigureActivity.TEXTE_GAUCHE:
					paint.setTextAlign(Paint.Align.LEFT);
					break;

				default:
					// Automatique
					if (pourcent < 0.5)
						paint.setTextAlign(Paint.Align.RIGHT);
					else
						paint.setTextAlign(Paint.Align.LEFT);
			}

			Rect rText = new Rect(r1);
			rText.inset(16, 2);
			drawCenterText(nom + ": " + nbMinutesConsommees + "/" + nbMinutesMax, rText, canvas, paint);
		}
	}

	// Calcule la progression dans le mois d'abonnement courant
	private static float calculProgressionDate(int debutAbo)
	{
		// Jour de debut de ce mois d'abonnement
		Calendar dateDebut = SIMS.getMoisPrecedent(Calendar.getInstance(), debutAbo);
		// Jour de fin de ce mois d'abonnement
		Calendar dateFin = (Calendar) dateDebut.clone();
		SIMS.MoisSuivant(dateFin);

		long debut = dateDebut.getTimeInMillis();
		long fin = dateFin.getTimeInMillis();
		long maintenant = Calendar.getInstance().getTimeInMillis();

		return (float) (maintenant - debut) / (float) (fin - debut);
	}


	private static int clair(int c)
	{
		return Color.argb(Color.alpha(c), composantClair(Color.red(c)), composantClair(Color.green(c)), composantClair(Color.blue(c)));
	}

	private static int composantClair(int c)
	{
		c = (int) (c + 1.7);
		if (c > 255)
			c = 255;
		return c;
	}

	private static int fonce(int c)
	{
		return Color.argb(Color.alpha(c), composantFonce(Color.red(c)), composantFonce(Color.green(c)), composantFonce(Color.blue(c)));
	}

	private static int composantFonce(int c)
	{
		return (int) (c * 0.2);
	}

	public static void drawCenterText(String text, Rect rectF, Canvas canvas, Paint paint)
	{
		Paint.Align align = paint.getTextAlign();
		float x;
		float y;
		//x
		if (align == Paint.Align.LEFT)
		{
			x = rectF.left;//.rectF.centerX() - paint.measureText(text) / 2;
		}
		else if (align == Paint.Align.CENTER)
		{
			x = rectF.centerX();
		}
		else
		{
			// Droite
			x = rectF.right;//- paint.measureText(text) / 2;
		}
		//y
		Paint.FontMetrics metrics = paint.getFontMetrics();
		float acent = Math.abs(metrics.ascent);
		float descent = Math.abs(metrics.descent);
		y = rectF.centerY() + (acent - descent) / 2f;
		canvas.drawText(text, x, y, paint);
	}

	protected static void RegisterOnClickListener(Context context, RemoteViews rv, int appWidgetId)
	{
		Intent intent = new Intent(context, ConsoDualSIM.class);
		intent.setAction(ACTION_CONFIGURE /*AppWidgetManager.ACTION_APPWIDGET_CONFIGURE*/);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(R.id.imageView, pendingIntent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		// There may be multiple widgets active, so update all of them
		for (int appWidgetId : appWidgetIds)
		{
			final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.conso_dual_sim);
			RegisterOnClickListener(context, rv, appWidgetId);
			RegisterPhoneStateListener(context);
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	static private void RegisterPhoneStateListener(final Context context)
	{
		tManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
		tManager.listen(new PhoneStateListener() {
			/**
			 * Callback invoked when device call state changes.
			 *
			 * @param state          call state
			 * @param incomingNumber incoming call phone number. If application does not have
			 *                       {@link Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE} permission, an empty
			 *                       string will be passed as an argument.
			 * @see TelephonyManager#CALL_STATE_IDLE
			 * @see TelephonyManager#CALL_STATE_RINGING
			 * @see TelephonyManager#CALL_STATE_OFFHOOK
			 */
			@Override
			public void onCallStateChanged(int state, String incomingNumber)
			{
				//super.onCallStateChanged(state, incomingNumber);
				if (lastState == state)
				{
					//No change, debounce extras
					return;
				}
				Log.d(TAG, "onCallStateChanged:");
				switch (state)
				{
					case TelephonyManager.CALL_STATE_RINGING:
						Log.d(TAG, "STATE_RINGING");
						isIncoming = true;
						break;
					case TelephonyManager.CALL_STATE_OFFHOOK:
						Log.d(TAG, "STATE_OFFHOOK");
						//Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
						if (lastState != TelephonyManager.CALL_STATE_RINGING)
						{
							isIncoming = false;
						}
						break;

					case TelephonyManager.CALL_STATE_IDLE:
						//Went to idle-  this is the end of a call.  What type depends on previous state(s)
						Log.d(TAG, "STATE_IDLE");
						if (lastState != TelephonyManager.CALL_STATE_RINGING && !isIncoming)
							{
								UpdateAll(context);
							}
						break;
				}

				lastState = state;
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);
	}

	static private void UpdateAll(Context context)
	{
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		ComponentName widgetComponent = new ComponentName(context, ConsoDualSIM.class);
		int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);

		Intent update = new Intent(context, ConsoDualSIM.class);
		update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		context.sendBroadcast(update);
	}

	// @Override
	@Override
	public void onReceive(Context context, Intent intent)
	{
		final String action = intent.getAction();
		Log.d(TAG, "OnReceive " + action);

		switch (action)
		{
			case ACTION_CONFIGURE:
				afficheConfiguration(context, intent);
				break;
		}
		//if (action.equals(Intent.ACTION_DATE_CHANGED))
		//	HandleDateChanged(context, intent);
		//else
		//if (action.equals(ACTION_CONFIGURE))
		//	afficheConfiguration(context, intent);
		//else if (action.equals(ConsommationTelephoneService.ACTION_CHANGED))
		//	ServiceUpdate(context, intent);
		//else if (action.contentEquals("com.motorola.blur.home.ACTION_SET_WIDGET_SIZE")) //$NON-NLS-1$
		//	HandleResize(context, intent);
		//else if (action.equals(ConsommationTelephoneService.ACTION_CONFIG))
		//	HandleConfigure(context, intent);
		super.onReceive(context, intent);
	}

	private void afficheConfiguration(Context context, Intent intent)
	{
		int WidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		Intent myIntent = new Intent(context, ConsoDualSIMConfigureActivity.class);
		myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		myIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetId);
		context.startActivity(myIntent);


		////
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		ComponentName widgetComponent = new ComponentName(context, getClass());
		int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);

		Intent update = new Intent(context, getClass());
		update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		context.sendBroadcast(update);
	}


	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		// When the user deletes the widget, delete the preference associated with it.
		for (int appWidgetId : appWidgetIds)
		{
			//ConsoDualSIMConfigureActivity.deleteTitlePref(context, appWidgetId);
			//RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.conso_dual_sim);
			AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetIds, appWidgetId);
		}
	}

	@Override
	public void onEnabled(Context context)
	{
		// Enter relevant functionality for when the first widget is created
	}

	@Override
	public void onDisabled(Context context)
	{
		// Enter relevant functionality for when the last widget is disabled
	}


}

