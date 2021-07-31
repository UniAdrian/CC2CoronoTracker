package de.uni.cc2coronotracker.ui.views.otp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.ui.views.MainActivity;

public class OtpConfigure extends AppCompatActivity {

    public CardView continuebutton;
    public EditText phone_input;
    String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_configure);
        continuebutton=findViewById(R.id.next);
        phone_input=findViewById(R.id.phone);

        continuebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(OtpConfigure.this,OtpScreen.class);
                number =phone_input.getText().toString();
                intent.putExtra("number",number);
                startActivity(intent);
            }
        });
    }

}