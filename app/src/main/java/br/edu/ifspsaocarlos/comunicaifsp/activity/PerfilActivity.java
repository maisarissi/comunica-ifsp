package br.edu.ifspsaocarlos.comunicaifsp.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import br.edu.ifspsaocarlos.comunicaifsp.Manifest;
import br.edu.ifspsaocarlos.comunicaifsp.R;
import br.edu.ifspsaocarlos.comunicaifsp.User;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by eu.nicekuba on 18/10/2017.
 */

public class PerfilActivity extends AppCompatActivity {

    private CircleImageView mPerfilImage;
    private EditText mPerfilName;
    private EditText mPerfilEmail;
    private EditText mPerfilPassword;
    private Button mButton;
    private Uri capturedUri;
    private File file;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        mPerfilImage = (CircleImageView) findViewById(R.id.perfil_image);
        mPerfilName = (EditText) findViewById(R.id.perfil_name);
        mPerfilEmail = (EditText) findViewById(R.id.perfil_email);
        mPerfilPassword = (EditText) findViewById(R.id.perfil_password);
        mButton = (Button) findViewById(R.id.perfil_button);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Editar Perfil");

        setupButtonPerfil();
    }

    private void setupButtonPerfil(){
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mButton.getText().toString().toLowerCase().equals("editar")){
                    enableFields();
                }else{
                    updateProfile();
                }
            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callCamera();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void setupImageClick(){
        mPerfilImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PerfilActivity.this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }else{
                        file = new File(Environment.getExternalStorageDirectory(), (Calendar.getInstance().getTime().getTime() + ".jpg" ));
                        if(!file.exists()){
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        capturedUri = Uri.fromFile(file);
                        callCamera();
                    }
                }else{
                    File file = new File(Environment.getExternalStorageDirectory(), (Calendar.getInstance().getTime().getTime() + ".jpg" ));
                    if(!file.exists()){
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    capturedUri = Uri.fromFile(file);
                   callCamera();
                }

            }
        });
    }


    private void callCamera(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 101);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101) {
            if(data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                MediaScannerConnection.scanFile(this, new String[]  {file.getPath()} , new String[]{"image/*"}, null);
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.JPEG,100,out);
                    out.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mPerfilImage.setImageBitmap(photo);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                if(mButton.getText().toString().toLowerCase().equals("editar")) {
                    onBackPressed();
                }else{
                    disableFields();
                }
                    break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void enableFields(){
        setupImageClick();
        mButton.setAlpha(1);
        mButton.setText("ATUALIZAR");
        mPerfilPassword.setVisibility(View.VISIBLE);
        mPerfilImage.setClickable(true);
        mPerfilEmail.setEnabled(true);
        mPerfilName.setEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);

    }

    private void disableFields(){
        mButton.setAlpha(0.5f);
        mButton.setText("EDITAR");
        mPerfilPassword.setVisibility(View.INVISIBLE);
        mPerfilImage.setClickable(false);
        mPerfilEmail.setEnabled(false);
        mPerfilName.setEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
    }

    private void updateProfile(){
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()+"/photo1.jpg");
        if(capturedUri != null){
            UploadTask task = ref.putFile(capturedUri);
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("TESTESS",e.getMessage());
                }
            });
        }


        DatabaseReference refMigue = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        if (mPerfilEmail.getText().length() != 0){
            refMigue.child("email").setValue(mPerfilEmail.getText().toString());
        }
        if (mPerfilPassword.getText().length() != 0){
            refMigue.child("password").setValue(mPerfilPassword.getText().toString());
        }
        if (mPerfilName.getText().length() != 0){
            refMigue.child("name").setValue(mPerfilName.getText().toString());
        }

        Toast.makeText(this, "Perfil atualizado com sucesso !", Toast.LENGTH_LONG).show();
        finish();
    }
}
