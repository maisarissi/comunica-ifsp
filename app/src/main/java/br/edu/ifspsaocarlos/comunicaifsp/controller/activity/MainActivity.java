package br.edu.ifspsaocarlos.comunicaifsp.controller.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import br.edu.ifspsaocarlos.comunicaifsp.model.entity.LibraryClass;
import br.edu.ifspsaocarlos.comunicaifsp.R;
import br.edu.ifspsaocarlos.comunicaifsp.model.entity.Topic;
import br.edu.ifspsaocarlos.comunicaifsp.model.entity.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends CommonActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DrawerLayout container;
    private NavigationView navigationView;
    private RecyclerView mRecycler;
    private TextView mDefaultMsg;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        createProgressDialog();

        showProgressDialog("Carregando");

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();

        configNavigationView();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.action_meusTopicos) {
                    Intent goToTopico = new Intent(MainActivity.this, TopicoActivity.class);
                    startActivity(goToTopico);
                }
                else if (id == R.id.action_logout){
                    showProgressDialog("Saindo");
                    editor.clear();
                    editor.commit();
                    firebaseAuth.signOut();
                    finish();
                }
                else if (id == R.id.action_meuPerfil) {
                    Intent goToProfile = new Intent(MainActivity.this, PerfilActivity.class);
                    startActivity(goToProfile);
                }
                else if (id == R.id.action_home) {
                    container.closeDrawer(GravityCompat.START);
                }

                return false;
            }
        });

        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
                        firebaseAuth.signOut();
                        finish();
                    }
                }
            }
        };

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(authStateListener);
        databaseReference = LibraryClass.getFirebase();
        databaseReference.getRef();


        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            getUserFlag();
            mRecycler.setAdapter(new FirebaseRecyclerAdapter<Topic, TopicoActivity.MyViewHolder>(Topic.class, R.layout.cell_topico,
                    TopicoActivity.MyViewHolder.class, databaseReference.child("usuario_topico").child(user.getUid())) {
                @Override
                protected void populateViewHolder(final TopicoActivity.MyViewHolder viewHolder, Topic model, int position) {
                    final Topic modelFinal = model;
                    final String[] creatorName = {""};
                    mDefaultMsg.setVisibility(View.GONE);
                    viewHolder.txt_name.setText("[" + model.getCourse().toUpperCase() + "] " + model.getName());
                    viewHolder.txt_msg.setText(model.getDescription());
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, TopicMessageActivity.class);
                            intent.putExtra("topic", modelFinal);
                            startActivity(intent);
                        }
                    });
                }
            });

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            mLayoutManager.setReverseLayout(true);
            mLayoutManager.setStackFromEnd(true);

            mRecycler.setLayoutManager(mLayoutManager);
            dismissProgressDialog();
        }
    }


    public void configNavigationView(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        View header =  navigationView.getHeaderView(0);
        final CircleImageView profileImage = (CircleImageView) header.findViewById(R.id.nav_image_profile);
        final TextView name = (TextView) header.findViewById(R.id.nav_name_label);
        TextView email = (TextView) header.findViewById(R.id.nav_email_label);

        if (user != null){
            if (user.getUid() != null) {
                DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("users");
                refUser.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User currentUser = dataSnapshot.getValue(User.class);

                        editor.putString("name", currentUser.getName());
                        editor.putString("email", currentUser.getEmail());
                        editor.commit();

                        name.setText(pref.getString("name", null));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            email.setText(user.getEmail());

            StorageReference ref = FirebaseStorage.getInstance().getReference();
            ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()+"/photo1.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(MainActivity.this).setLoggingEnabled(true);
                    Picasso.with(MainActivity.this).load(uri).placeholder(getResources().getDrawable(R.mipmap.ic_launcher_round)).into(profileImage);
                }
            });
        }

    }

    private void createProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void showProgressDialog (String text){
        progressDialog.setMessage(text);
        progressDialog.show();
    }

    private void dismissProgressDialog (){
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgressDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            editor.clear();
            editor.commit();
            FirebaseAuth.getInstance().signOut();
            finish();
        }
        else if (id == android.R.id.home){
            if(container.isDrawerOpen(GravityCompat.START)){
                container.closeDrawer(GravityCompat.START);
            }else{
                container.openDrawer(GravityCompat.START);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initViews() {
        container = (DrawerLayout) findViewById(R.id.main_container);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        mRecycler = (RecyclerView) findViewById(R.id.meus_topicos);
		mDefaultMsg = (TextView) findViewById(R.id.default_msg);
    }

    @Override
    protected void initObject() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
