/**
 * 
 */
package pilloni.lucien.consommationdualsim.colorpicker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import pilloni.lucien.consommationdualsim.R;
import pilloni.lucien.consommationdualsim.colorpicker.ColorPicker;
import pilloni.lucien.consommationdualsim.colorpicker.OpacityBar;
import pilloni.lucien.consommationdualsim.colorpicker.SaturationBar;
import pilloni.lucien.consommationdualsim.colorpicker.ValueBar;

/**
 * @author lucien
 * 
 */
public class ColorPickerActivity extends Activity
{

	public static final String COULEUR = "couleur"; //$NON-NLS-1$
	public static final int RESULT_CODE_COULEUR = 1;
	public static final String TYPE_COULEUR = "typecouleur"; //$NON-NLS-1$
	private String _typeCouleur = "inconnu" ; //$NON-NLS-1$

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setResult(RESULT_CANCELED);
		// Inflate our UI from its XML layout description.
		setContentView(R.layout.colorpicker);

		ColorPicker picker = (ColorPicker) findViewById(R.id.colorPicker);
		OpacityBar opacityBar = (OpacityBar) findViewById(R.id.opacityBar);
		SaturationBar saturationBar = (SaturationBar) findViewById(R.id.saturationBar);
		ValueBar valueBar = (ValueBar) findViewById(R.id.valueBar);

		picker.addOpacityBar(opacityBar);
		picker.addSaturationBar(saturationBar);
		picker.addValueBar(valueBar);

		Bundle b = getIntent().getExtras() ;
		if (b != null)
		{
			int Couleur = b.getInt(COULEUR);
			picker.setColor(Couleur);
			_typeCouleur = b.getString(TYPE_COULEUR);
		}
	}

	public void OnOK(View v)
	{
		ColorPicker picker = (ColorPicker) findViewById(R.id.colorPicker);
		Intent resultValue = new Intent();
		
		resultValue.putExtra(TYPE_COULEUR, _typeCouleur);
		resultValue.putExtra(COULEUR, picker.getColor());
		setResult(RESULT_OK, resultValue);
		finish();
	}

	public void OnAnnuler(View v)
	{
		setResult(RESULT_CANCELED, new Intent());
		finish();
	}

	public void onClickBoutonCouleur(View v)
	{
		ColorPicker picker = (ColorPicker) findViewById(R.id.colorPicker);
		int nouvelleCouleur = ((ColorDrawable) v.getBackground()).getColor();
		int ancienneCouleur =  picker.getColor();

		nouvelleCouleur = Color.argb( Color.alpha(ancienneCouleur), Color.red(nouvelleCouleur), Color.green(nouvelleCouleur), Color.blue(nouvelleCouleur));
		picker.setColor(nouvelleCouleur);
	}
}
