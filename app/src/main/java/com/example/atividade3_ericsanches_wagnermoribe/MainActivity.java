package com.example.atividade3_ericsanches_wagnermoribe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView distanciaInputTextView;
    private TextInputEditText procurarInputText;
    private Chronometer tempoPercorridoChronometer;
    private boolean gpsAtivado = false;
    private boolean iniciarRota = false;
    private Button permisaoButton;
    private Button ativarBotaoGps;
    private Button desativarBotaoGps;
    private Button iniciarPercursoBotao;
    private Button terminarPercursoBotao;
    private FloatingActionButton procurarBotao;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final int REQUEST_GPS_CODE = 1001;

    private Location localidadeAnterior;

    private double distancia = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciar();


        permisaoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_GPS_CODE);
                gpsAtivado = true;
            }
        });

        desativarBotaoGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gpsAtivado)
                    desativarGPS();
                else
                    Toast.makeText(MainActivity.this, getString(R.string.gpsdesativado), Toast.LENGTH_SHORT).show();
            }
        });

        ativarBotaoGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ativarGPS();
            }
        });

        iniciarPercursoBotao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gpsAtivado){
                    iniciarRota = true;
                    distanciaInputTextView.setText(String.format(Locale.getDefault(),"%.1f KM",distancia/1000));
                    tempoPercorridoChronometer.setBase(SystemClock.elapsedRealtime());
                    tempoPercorridoChronometer.start();
                }
                else
                    Toast.makeText(MainActivity.this, getString(R.string.precisaativarGPS), Toast.LENGTH_SHORT).show();

            }
        });

        terminarPercursoBotao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarRota = false;
                distanciaInputTextView.setText(String.format(Locale.getDefault(),"%.1f KM",distancia/1000));
                long elapsedMillis = SystemClock.elapsedRealtime() - tempoPercorridoChronometer.getBase();
                tempoPercorridoChronometer.stop();
                tempoPercorridoChronometer.setBase(SystemClock.elapsedRealtime());
                Toast.makeText(MainActivity.this, String.format(Locale.getDefault(),"Distancia Percorrida = %.2f KM\nTempo Percorrido = %s",
                        distancia/1000, tempoPercorrido(elapsedMillis)), Toast.LENGTH_SHORT).show();
                distancia = 0.0;
                distanciaInputTextView.setText("0.0 KM");
            }
        });

        procurarBotao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String procurar = "geo:"+localidadeAnterior.getAltitude()+","+localidadeAnterior.getLongitude()+"?q="+procurarInputText.getText();
                Uri uri = Uri.parse(String.format(Locale.getDefault(),procurar));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });

    }

    private String tempoPercorrido(long millis){
        return String.format(Locale.getDefault(),"%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private void iniciar() {
        permisaoButton = findViewById(R.id.condecederPermisaoButton);
        ativarBotaoGps = findViewById(R.id.ativarGPSButton);
        desativarBotaoGps = findViewById(R.id.desativarGPSButton);
        iniciarPercursoBotao = findViewById(R.id.iniciarPercursoButton);
        terminarPercursoBotao = findViewById(R.id.terminarPercursoButton);
        procurarBotao = findViewById(R.id.procurarButton);
        procurarInputText = findViewById(R.id.procurarInputText);

        localidadeAnterior = new Location("Distancia");

        distanciaInputTextView = findViewById(R.id.distanciaInputTextView);
        tempoPercorridoChronometer = findViewById(R.id.tempoPercorridoChronometer);
        tempoPercorridoChronometer.setBase(SystemClock.elapsedRealtime());
        tempoPercorridoChronometer.stop();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(iniciarRota){
                    if(localidadeAnterior!= null)
                        distancia += location.distanceTo(localidadeAnterior);
                    distanciaInputTextView.setText(String.format(Locale.getDefault(),"%.1f KM",distancia/1000));
                    localidadeAnterior.setAltitude(location.getAltitude());
                    localidadeAnterior.setLongitude(location.getLongitude());
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(MainActivity.this, provider, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void ativarGPS() {
        if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
            gpsAtivado = true;
        }
        else{
            Toast.makeText(MainActivity.this, getString(R.string.concedapermissao), Toast.LENGTH_SHORT).show();
        }
    }

    private void desativarGPS(){
        locationManager.removeUpdates(locationListener);
        gpsAtivado = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_GPS_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
                }
                else{
                    Toast.makeText(this, getString(R.string.semgps), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}