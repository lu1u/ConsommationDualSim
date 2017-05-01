package pilloni.lucien.consommationdualsim;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.v4.app.ActivityCompat;

import java.util.Calendar;

/**
 * Created by lucien on 27/04/2017.
 */

class SIMS {
	//static final String TAG = "Database";
	static private final String NUMEROS_GRATUITS = "( '666', '+33695600011' )";
	static private final String[] COLONNES_COMPTE_APPELS = {android.provider.CallLog.Calls.DURATION};

	/***
	 * Retourne le nombre de minutes consommees depuis le debut
	 * @param context
	 * @param noSIM
	 * @param debutAbo
	 * @return
	 */
	public static int getConsommation(Context context, int noSIM, int debutAbo)
	{
		int nbSecTelephone = 0;
		Calendar dateChangementAbonnement = getMoisPrecedent(Calendar.getInstance(), debutAbo);

		try
		{
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
			{
				return 0;
			}
/*
			Cursor cur = context.getContentResolver().query(
					android.provider.CallLog.Calls.CONTENT_URI,
					null,null,							null,
					android.provider.CallLog.Calls.DATE + " DESC");

			String res = "";
			for (int i = 0;i < cur.getColumnCount();i++)
			{
				res += formate(cur.getColumnName(i)) ;
			}

			Log.v(TAG, "--------------------------------------------------------------------------------------------------------------------");
			Log.v(TAG, res);
			Log.v(TAG, "--------------------------------------------------------------------------------------------------------------------");
			cur.moveToFirst();
			while (cur.moveToNext())
			{
				res = "";
				for (int i = 0;i < cur.getColumnCount();i++)
				{
					res += formate(cur.getString(i));
				}

				Log.v(TAG, res);
			}*/
			Cursor cur = context.getContentResolver().query(
					android.provider.CallLog.Calls.CONTENT_URI,
					COLONNES_COMPTE_APPELS,
					android.provider.CallLog.Calls.DATE + " >= " + dateChangementAbonnement.getTimeInMillis()
							+ " AND " + android.provider.CallLog.Calls.TYPE + " = " + android.provider.CallLog.Calls.OUTGOING_TYPE
							+ " AND SUBSCRIPTION_ID = '" + noSIM + "'"
							+ " AND " + android.provider.CallLog.Calls.NUMBER + " NOT IN " + NUMEROS_GRATUITS,
					null,
					null);

			if (cur != null)
			{
				final int DureeCol = cur.getColumnIndex(android.provider.CallLog.Calls.DURATION);

				while (cur.moveToNext())
				{
					long duration = cur.getLong(DureeCol);
					nbSecTelephone += duration;
				}

				cur.close();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return nbSecTelephone / 60;
	}

	/*
		private static String formate(String v)
		{
			if( v == null)
				v = "          ";
			if ( v.length()> 10)
				return v.substring(0,10) + '|';

			while (v.length()< 10)
				v += " ";

			return v + '|';
		}
	*/
	public static void MoisPrecedent(Calendar date)
	{
		if (date.get(Calendar.MONTH) > Calendar.JANUARY)
			date.add(Calendar.MONTH, -1);
		else
		{
			date.add(Calendar.YEAR, -1);
			date.set(Calendar.MONTH, Calendar.DECEMBER);
		}

		if (date.get(Calendar.DAY_OF_MONTH) > date.getActualMaximum(Calendar.DAY_OF_MONTH))
			date.set(Calendar.DAY_OF_MONTH,
					date.getActualMaximum(Calendar.DAY_OF_MONTH));
	}

	public static void MoisSuivant(Calendar date)
	{
		if (date.get(Calendar.MONTH) < Calendar.DECEMBER)
			date.add(Calendar.MONTH, 1);
		else
		{
			date.add(Calendar.YEAR, 1);
			date.set(Calendar.MONTH, Calendar.JANUARY);
		}

		if (date.get(Calendar.DAY_OF_MONTH) > date.getActualMaximum(Calendar.DAY_OF_MONTH))
			date.set(Calendar.DAY_OF_MONTH,
					date.getActualMaximum(Calendar.DAY_OF_MONTH));
	}

	public static Calendar getMoisPrecedent(Calendar D, int dateDebut)
	{
		Calendar Date = (Calendar) D.clone();

		if (Date.get(Calendar.DAY_OF_MONTH) >= dateDebut)
		{
			Date.set(Calendar.DAY_OF_MONTH, dateDebut);
		}
		else
		{
			MoisPrecedent(Date);

			Date.set(Calendar.DAY_OF_MONTH,
					Math.min(dateDebut, Date.getActualMaximum(Calendar.DAY_OF_MONTH)));
		}

		Date.set(Calendar.HOUR_OF_DAY, 0);
		Date.set(Calendar.MINUTE, 0);
		Date.set(Calendar.SECOND, 0);

		return Date;
	}
}
