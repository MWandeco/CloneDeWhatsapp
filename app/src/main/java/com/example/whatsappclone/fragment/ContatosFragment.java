package com.example.whatsappclone.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.whatsappclone.R;
import com.example.whatsappclone.activity.ChatActivity;
import com.example.whatsappclone.activity.GrupoActivity;
import com.example.whatsappclone.adapter.ContatosAdapter;
import com.example.whatsappclone.adapter.ConversasAdapter;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.helper.RecyclerItemClickListener;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Conversa;
import com.example.whatsappclone.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewListaContatos;
    private ContatosAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;
    private ImageView imageGrupo;

    public ContatosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        //Configurações iniciais
        recyclerViewListaContatos = view.findViewById(R.id.recyclerViewListaContatos);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();
        imageGrupo = view.findViewById(R.id.imageGrupo);

        //configurar adapter
        adapter = new ContatosAdapter(listaContatos, getActivity() );

        //configurar recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getActivity() );
        recyclerViewListaContatos.setLayoutManager( layoutManager );
        recyclerViewListaContatos.setHasFixedSize( true );
        recyclerViewListaContatos.setAdapter( adapter );

        //Configurar evento de clique no recyclerview
        recyclerViewListaContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListaContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Usuario> listaUsuarioAtualizada = adapter.getContatos();
                                Usuario usuarioSelecionado = listaUsuarioAtualizada.get( position );
                                Intent i = new Intent(getActivity(), ChatActivity.class);
                                i.putExtra("chatContato", usuarioSelecionado );
                                startActivity( i );

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

        imageGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), GrupoActivity.class);
                startActivity(i);
            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener( valueEventListenerContatos );
    }

    public void recuperarContatos(){
        listaContatos.clear();
        valueEventListenerContatos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaContatos.clear();
                for ( DataSnapshot dados: dataSnapshot.getChildren() ){

                    Usuario usuario = dados.getValue( Usuario.class );

                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if ( !emailUsuarioAtual.equals( usuario.getEmail() ) ){
                        listaContatos.add( usuario );
                    }


                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    public void pesquisarContatos(String texto){
        List<Usuario> listaContatosBusca = new ArrayList<>();
        for(Usuario usuario : listaContatos){
                String nome = usuario.getNome().toLowerCase();
                if(nome.contains(texto)){
                    listaContatosBusca.add(usuario);
                }


        }
        adapter = new ContatosAdapter(listaContatosBusca,getActivity());
        recyclerViewListaContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    public void recarregarContatos(){
        adapter = new ContatosAdapter(listaContatos,getActivity());
        recyclerViewListaContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}