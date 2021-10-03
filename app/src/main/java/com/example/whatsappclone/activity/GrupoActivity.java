package com.example.whatsappclone.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.example.whatsappclone.R;
import com.example.whatsappclone.adapter.ContatosAdapter;
import com.example.whatsappclone.adapter.GrupoSelecionadoAdapter;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.helper.RecyclerItemClickListener;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrupoActivity extends AppCompatActivity {
    private RecyclerView recyclerMembrosSelecionados,recyclerMembros;
    private ContatosAdapter contatosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference usuariosRef;
    private FirebaseUser usuarioAtual;
    private Toolbar toolbar1;
    private FloatingActionButton fabAvancarCadastro;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);

        //configurar toolbar
        toolbar1 = findViewById(R.id.toolbar1);
        toolbar1.setTitle(R.string.grupo);

        setSupportActionBar(toolbar1);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fabAvancarCadastro = findViewById(R.id.fabAvancarCadastro);

        // configurção de membros da lista
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        recyclerMembros = findViewById(R.id.recyclerMembros);
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosSelecionados);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        contatosAdapter = new ContatosAdapter(listaMembros, getApplicationContext());
        recyclerMembros.setLayoutManager(layoutManager);
        recyclerMembros.setHasFixedSize(true);
        recyclerMembros.setAdapter(contatosAdapter);

        recyclerMembros.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerMembros,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onItemClick(View view, int position) {
                                Usuario usuarioSelecionado = listaMembros.get(position);
                                //remove usuario selecionado da lista de amigos
                                listaMembros.remove(usuarioSelecionado);
                                contatosAdapter.notifyDataSetChanged();

                                //adiciona membros selecionados na lista do grupo
                                listaMembrosSelecionados.add(usuarioSelecionado);
                                grupoSelecionadoAdapter.notifyDataSetChanged();

                                // atualiza os membros da toolbar
                                atualizarMembrosToolbar();

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                ));
        //configuraçao do recyclerview de membros selecionados
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados,getApplicationContext());
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),LinearLayoutManager.HORIZONTAL,false
        );
        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        recyclerMembrosSelecionados.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(), recyclerMembrosSelecionados, new RecyclerItemClickListener.OnItemClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecionado = listaMembrosSelecionados.get(position);
                        //remover membros selecionados
                        listaMembrosSelecionados.remove(usuarioSelecionado);
                        grupoSelecionadoAdapter.notifyDataSetChanged();
                        listaMembros.add(usuarioSelecionado);
                        contatosAdapter.notifyDataSetChanged();

                        // atualiza os membros da toolbar
                        atualizarMembrosToolbar();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
                )
        );
        //configurar o Floating Action Button
        fabAvancarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GrupoActivity.this,CadastroGrupoActivity.class);
                i.putExtra("Membros",(Serializable) listaMembrosSelecionados);
                startActivity(i);
            }
        });

    }

    public void atualizarMembrosToolbar(){
        int totalSelecionado = listaMembrosSelecionados.size();
        int total = listaMembros.size() + listaMembrosSelecionados.size();
        toolbar1.setSubtitle(totalSelecionado +" de "+ total + " Selecionados");
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener( valueEventListenerMembros );
    }
    public void recuperarContatos(){

        valueEventListenerMembros = usuariosRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaMembros.clear();
                for ( DataSnapshot dados: dataSnapshot.getChildren() ){

                    Usuario usuario = dados.getValue( Usuario.class );

                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if ( !emailUsuarioAtual.equals( usuario.getEmail() ) ){
                        listaMembros.add( usuario );
                    }


                }

                contatosAdapter.notifyDataSetChanged();
                // atualiza os membros da toolbar
                atualizarMembrosToolbar();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}