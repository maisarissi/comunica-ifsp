package br.edu.ifspsaocarlos.comunicaifsp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import br.edu.ifspsaocarlos.comunicaifsp.R;
import br.edu.ifspsaocarlos.comunicaifsp.Topic;
import br.edu.ifspsaocarlos.comunicaifsp.TopicoAdapter;
import br.edu.ifspsaocarlos.comunicaifsp.view.TopicPresenter;

/**
 * Created by MRissi on 15-Sep-17.
 */

public class TopicoActivity extends AppCompatActivity implements TopicPresenter {
    RecyclerView rV;
    private DrawerLayout container;
    private NavigationView navigationView;
    FirebaseRecyclerAdapter<Topic, MyViewHolder> firebaseRecyclerAdapter;

    private FloatingActionButton btnNovoTopico;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topico);

        btnNovoTopico = (FloatingActionButton) findViewById(R.id.btn_callNovoTopico);
        container = (DrawerLayout) findViewById(R.id.topico_container);
        rV = (RecyclerView) findViewById(R.id.recycler_view);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.action_home) {
                    Intent goToMain = new Intent(TopicoActivity.this, MainActivity.class);
                    startActivity(goToMain);
                }
                else if (id == R.id.action_logout){
                    FirebaseAuth.getInstance().signOut();
                }

                return false;
            }
        });

//        ArrayList<Topic> list = new ArrayList<>();
//        Topic celula = new Topic(this);
//        celula.setName("2017-2-PRJT6-648");
//        celula.setDescription("ADS - Disciplina de PRJ");
//        list.add(celula);
//
//        TopicoAdapter adapter = new TopicoAdapter(list);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Topic, MyViewHolder>(Topic.class, R.layout.cell_topico,
                MyViewHolder.class, databaseReference.child("generalTopic")) {
            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, Topic model, int position) {
                final Topic modelFinal = model;
                viewHolder.txt_name.setText(model.getName());
                viewHolder.txt_msg.setText(model.getDescription());
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //modelFinal
                    }
                });
            }
        };

        rV.setAdapter(firebaseRecyclerAdapter);
        rV.setLayoutManager(new LinearLayoutManager(this));

        getSupportActionBar().setTitle("Tópicos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        btnNovoTopico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopicoActivity.this , CadastroTopicoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }
        else if (id == android.R.id.home){
            container.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showMessageToast(String message) {
        Toast.makeText(this,
                message,
                Toast.LENGTH_LONG)
                .show();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView txt_name;
        TextView txt_msg;

        public MyViewHolder(View itemView) {
            super(itemView);
            txt_name = (TextView) itemView.findViewById(R.id.client_name);
            txt_msg = (TextView) itemView.findViewById(R.id.client_msg);
        }
    }
}
