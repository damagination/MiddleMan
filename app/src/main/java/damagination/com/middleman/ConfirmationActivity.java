package damagination.com.middleman;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class ConfirmationActivity extends ActionBarActivity {

    TextView tvPhoneNumberReport, tvEmailReport, tvBankAccountReport, tvPaymentStatusReport, tvPaymentConfirmationReport, tvFullNameReport, tvConfirmationIDReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWidgets();
        Bundle extras = getIntent().getExtras();

        setContentView(R.layout.activity_confirmation);
        initWidgets();
        buildUI(extras);
    }

    public void initWidgets() {
        tvBankAccountReport = (TextView) findViewById(R.id.tvBankAccountReport);
        tvPhoneNumberReport = (TextView) findViewById(R.id.tvPhoneReport);
        tvEmailReport = (TextView) findViewById(R.id.tvEmailReport);
        tvPaymentConfirmationReport = (TextView) findViewById(R.id.tvPaymentConfirmationReport);
        tvPaymentStatusReport = (TextView) findViewById(R.id.tvPaymentStatusReport);
        tvFullNameReport = (TextView) findViewById(R.id.tvFullNameReport);
        tvConfirmationIDReport = (TextView) findViewById(R.id.tvConfirmationIDReport);
    }

    public void buildUI(Bundle extras) {
        Boolean status = extras.getBoolean("RESULT");
        if (!status) {
            tvPaymentConfirmationReport.setText(Html.fromHtml("<font color='#FF6600'><b>Sorry, Payment was NOT SUCCESSFUL! Please check your payment source</b></font>"));
            tvPaymentStatusReport.setText(Html.fromHtml("Status: " + "<font color='#FF6600'><b>FAILED!</b></font>"));
            tvConfirmationIDReport.setVisibility(View.GONE);
        } else {
            tvPaymentConfirmationReport.setText(Html.fromHtml("<font color='#FF6600'><b>Congratulations, Payment was SUCCESSFUL!</b></font>"));
            tvPaymentStatusReport.setText(Html.fromHtml("Status: " + "<font color='#FF6600'><b>SUCCESS!</b></font>"));
            String id = extras.getString("ID");
            tvConfirmationIDReport.setVisibility(View.VISIBLE);
            tvConfirmationIDReport.setText(Html.fromHtml("Confirmation ID: " + "<font color='#FF6600'><b>" + id + "</b></font>"));
        }

        Boolean delivery = extras.getBoolean("BANK");
        String deliveryDetails = extras.getString("DELIVERY");
        if (!delivery) {
            tvBankAccountReport.setVisibility(View.GONE);
            tvPhoneNumberReport.setVisibility(View.VISIBLE);
            tvPhoneNumberReport.setText(Html.fromHtml("Sent via M-Pesa to: " + "<font color='#FF6600'><b>" + deliveryDetails + "</b></font>"));
        } else {
            tvPhoneNumberReport.setVisibility(View.GONE);
            tvBankAccountReport.setVisibility(View.VISIBLE);
            tvBankAccountReport.setText(Html.fromHtml("Sent to CRDB Bank Account: " + "<font color='#FF6600'><b>" + deliveryDetails + "</b></font>"));
        }

        String name = extras.getString("NAME");
        String email = extras.getString("EMAIL");

        tvFullNameReport.setText(Html.fromHtml("Receiver's Name: " + "<font color='#FF6600'><b>" + name + "</b></font>"));
        tvEmailReport.setText(Html.fromHtml("Receiver's Email: " + "<font color='#FF6600'><b>" + email + "</b></font>"));


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirmation, menu);
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
}
