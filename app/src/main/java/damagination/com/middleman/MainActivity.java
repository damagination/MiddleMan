package damagination.com.middleman;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, TextWatcher {

    EditText etAmount;
    Button btNext;
    TextView tvTotalAmount, tvAmountToSend, tvExchangeRate, tvServiceCharge, tvServiceChargesFootNote;
    BigDecimal bdTodaysRate, bdAmountToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateExchangeRate();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    public void updateExchangeRate() {
        ExchangeRate exrt = new ExchangeRate();
        exrt.execute();
    }

    //Initialize all Widgets and put initial texts
    public void initWidgets() {
        //Initialize widgets
        etAmount = (EditText) findViewById(R.id.etAmount);
        btNext = (Button) findViewById(R.id.btCash);
        tvTotalAmount = (TextView) findViewById(R.id.tvTotal);
        tvAmountToSend = (TextView) findViewById(R.id.tvTotalAmount);
        tvExchangeRate = (TextView) findViewById(R.id.tvExchangeRate);
        tvServiceCharge = (TextView) findViewById(R.id.tvServiceCharge);
        tvServiceChargesFootNote = (TextView) findViewById(R.id.tvServiceChargeFootNote);

        //Set initial texts
        BigDecimal initialBalance = new BigDecimal("0");

        tvTotalAmount.setText(Html.fromHtml("Total Amount: " + "<font color='#FF6600'><b>" + currencyFormat(initialBalance) + "</b></font>"));
        tvServiceCharge.setText(Html.fromHtml("<sup>*</sup>" + "Service Charge: " + "<font color='#FF6600'><b>" + currencyFormat(initialBalance) + "</b></font>"));
        tvAmountToSend.setText(Html.fromHtml("Total to be Sent: " + "<font color='#FF6600'><b>" + currencyFormat(initialBalance) + "</b></font>"));
        tvServiceChargesFootNote.setText(Html.fromHtml("<sup>*</sup>") + "damagination.com/middleman");

        //Add event listeners
        etAmount.addTextChangedListener(this);
        btNext.setOnClickListener(this);

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btCash:

                if (!etAmount.getText().toString().isEmpty()) {

                    BigDecimal max = new BigDecimal("300");
                    BigDecimal min = new BigDecimal("10");

                    if (bdAmountToSend.compareTo(max) == 1) {
                        Toast.makeText(this, "Maximum allowed etAmount is $300", Toast.LENGTH_LONG).show();
                        break;
                    } else if (bdAmountToSend.compareTo(min) == -1) {
                        Toast.makeText(this, "Minimum allowed etAmount is $10", Toast.LENGTH_LONG).show();
                        break;
                    } else {

                        Intent intent = new Intent(MainActivity.this, ReceiverActivity.class);
                        intent.putExtra("AMOUNT", bdAmountToSend.toString());
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Amount cannot be empty", Toast.LENGTH_LONG).show();
                }

                break;
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        if (!etAmount.getText().toString().trim().isEmpty()) {

            BigDecimal amount$ = new BigDecimal(etAmount.getText().toString());
            bdAmountToSend = amount$;

            BigDecimal valueTZS = amount$.multiply(bdTodaysRate);
            valueTZS = valueTZS.setScale(2, RoundingMode.CEILING);


            BigDecimal servicePercentCharge = new BigDecimal("0.05");
            BigDecimal serviceTZS = valueTZS.multiply(servicePercentCharge);
            serviceTZS = serviceTZS.setScale(2, RoundingMode.CEILING);

            BigDecimal totalReceivedTZS = valueTZS.subtract(serviceTZS);
            totalReceivedTZS = totalReceivedTZS.setScale(2, RoundingMode.CEILING);

            tvTotalAmount.setText(Html.fromHtml("Total Amount: " + "<font color='#FF6600'><b>" + currencyFormat(valueTZS) + "</b></font>"));
            tvServiceCharge.setText(Html.fromHtml("<sup>*</sup>" + "Service Charge: " + "<font color='#FF6600'><b>" + currencyFormat(serviceTZS) + "</b></font>"));
            tvAmountToSend.setText(Html.fromHtml("Total to be Sent: " + "<font color='#FF6600'><b>" + currencyFormat(totalReceivedTZS) + "</b></font>"));

        }
    }

    public String currencyFormat(BigDecimal bd) {

        Locale sw_TZ = new Locale("sw", "TZ");
        return NumberFormat.getCurrencyInstance(sw_TZ).format(bd);
    }

    public class ExchangeRate extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = ExchangeRate.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String exchangeRateJSON = null;

            try {

                final String exchangeRateURL = "http://rate-exchange.appspot.com/currency?from=USD&to=TZS";


                //URL to OWM query
                URL url = new URL(exchangeRateURL.toString());

                //create connection to OWM and open connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //Read input stream into String
                InputStream is = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (is == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                exchangeRateJSON = buffer.toString();
                return getExchangeRateDataFromJson(exchangeRateJSON);

            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Malformed URL", e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
                exchangeRateJSON = null;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON Parsing error", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        private String getExchangeRateDataFromJson(String exchangeRateJsonStr)
                throws JSONException {

            final String RATE = "rate";


            JSONObject exchangeRateJson = new JSONObject(exchangeRateJsonStr);
            String exchangeRate = String.valueOf(exchangeRateJson.getDouble(RATE));

            return exchangeRate;
        }

        @Override
        protected void onPostExecute(String results) {
            if (results != null) {
                bdTodaysRate = new BigDecimal(results);
                tvExchangeRate.setText(Html.fromHtml("Today's Exchange Rate: " + "<font color='#FF6600'><b>" + currencyFormat(bdTodaysRate) + "</b></font>"));
            } else {
                tvExchangeRate.setText(Html.fromHtml("Today's Exchange Rate: " + "<font color='red'>" + " ERROR </font>"));
                Toast.makeText(getApplicationContext(), "Please connect to the internet", Toast.LENGTH_LONG).show();
            }
        }
    }
}
