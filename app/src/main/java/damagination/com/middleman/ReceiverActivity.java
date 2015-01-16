package damagination.com.middleman;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;


public class ReceiverActivity extends ActionBarActivity implements View.OnClickListener {


    /**
     * - Set to PayPalConfiguration.ENVIRONMENT_PRODUCTION to move real money.
     * <p/>
     * - Set to PayPalConfiguration.ENVIRONMENT_SANDBOX to use your test credentials
     * from https://developer.paypal.com
     * <p/>
     * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
     * without communicating to PayPal's servers.
     */
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;
    private static final String CONFIG_CLIENT_ID = "Insert your auth key here";
    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
    private static final int REQUEST_CODE_PROFILE_SHARING = 3;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
                    // The following are only used in PayPalFuturePaymentActivity.
            .merchantName("Damagination")
            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));
    Spinner spBanksList;
    RadioButton rbMpesa, rbBankAccount;
    EditText etPhoneNumber, etAccountNumber, etFullName, etEmail;
    Button btContinue;
    BigDecimal bdAmountToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_contact);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initWidgets();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String tmp = extras.getString("AMOUNT");
            bdAmountToSend = new BigDecimal(tmp);
        }


        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
    }

    public void initWidgets() {
        spBanksList = (Spinner) findViewById(R.id.spBanks);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.banks_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBanksList.setAdapter(adapter);
        rbMpesa = (RadioButton) findViewById(R.id.rbMpesa);
        rbBankAccount = (RadioButton) findViewById(R.id.rbBank);
        etPhoneNumber = (EditText) findViewById(R.id.etPhone);
        etAccountNumber = (EditText) findViewById(R.id.etAccount);
        etFullName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);

        btContinue = (Button) findViewById(R.id.btSend);
        btContinue.setOnClickListener(this);


        //Default to M-Pesa Selected
        rbMpesa.setChecked(true);
        rbBankAccount.setChecked(false);
        etAccountNumber.setEnabled(false);
        spBanksList.setEnabled(false);
        etPhoneNumber.setEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_receiver, menu);
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

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btSend:

                if(etFullName.getText().toString().trim().isEmpty()){
                    Toast.makeText(this, "Name is required", Toast.LENGTH_LONG).show();
                    break;
                }

                if(!validatePhoneNumber(etPhoneNumber.getText().toString())){
                    Toast.makeText(this, "Phone number not correct", Toast.LENGTH_LONG).show();
                    break;
                }

                PayPalPayment transfer = new PayPalPayment(bdAmountToSend, "USD", "PayPal to Cash", PayPalPayment.PAYMENT_INTENT_SALE);
                Intent intent = new Intent(ReceiverActivity.this, PaymentActivity.class);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, transfer);
                startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                break;
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.rbBank:
                if (checked) {
                    rbMpesa.setChecked(false);
                    etPhoneNumber.setEnabled(false);
                    etAccountNumber.setEnabled(true);
                    spBanksList.setEnabled(true);
                }
                break;

            case R.id.rbMpesa:
                if (checked) {
                    rbBankAccount.setChecked(false);
                    etAccountNumber.setEnabled(false);
                    spBanksList.setEnabled(false);
                    etPhoneNumber.setEnabled(true);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent(ReceiverActivity.this, ConfirmationActivity.class);
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data
                        .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i("Payment", confirm.toJSONObject().toString());

                        JSONObject jsonObj = new JSONObject(confirm.toJSONObject().toString());

                        String paymentId = jsonObj.getJSONObject("response").getString("id");
                        Log.i("Payment", paymentId);

                        intent.putExtra("RESULT", true);
                        intent.putExtra("ID", paymentId);
                    } catch (JSONException e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                Log.i("Payment", "Canceled by User.");
                intent.putExtra("RESULT", false);
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Toast.makeText(this, "An error ocured, please try again", Toast.LENGTH_SHORT).show();
                Log.i("Payment", "An invalid Payment was submitted. Please see the docs.");
                intent.putExtra("RESULT", false);
            }

            intent.putExtra("NAME", etFullName.getText().toString());
            intent.putExtra("EMAIL", etEmail.getText().toString());
            if(rbBankAccount.isChecked()){
                intent.putExtra("BANK", true);
                intent.putExtra("DELIVERY", etAccountNumber.getText().toString());
            } else {
                intent.putExtra("BANK", false);
                intent.putExtra("DELIVERY", etPhoneNumber.getText().toString());
            }
        }
        startActivity(intent);
    }

    public boolean validatePhoneNumber(String number){
        if(number.length() != 13){
            return false;
        }
        if(!number.startsWith("+255")){
            //Toast.makeText("Country not supported")
            return false;
        }

        return true;
    }

    public boolean validateBankAccount(){
        return true;
    }

    public boolean validateName(){
        return true;
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

}
