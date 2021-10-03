package com.example.whatsappclone.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.example.whatsappclone.R;
import com.example.whatsappclone.activity.ChatActivity;
import com.example.whatsappclone.adapter.ContatosAdapter;
import com.example.whatsappclone.adapter.ConversasAdapter;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.helper.RecyclerItemClickListener;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Conversa;
import com.example.whatsappclone.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ConversasFragment extends Fragment {

    private RecyclerView recyclerListaConversas;
    private ConversasAdapter adapter;
    private ArrayList<Conversa> listaConversas = new ArrayList<>();
    private DatabaseReference database;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;


    public ConversasFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        //Configurações iniciais
        recyclerListaConversas = view.findViewById(R.id.recyclerListaConversas);
        database = ConfiguracaoFirebase.getFirebaseDatabase();

        //configurar adapter
        adapter = new ConversasAdapter(listaConversas,getActivity());

        //configurar recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getActivity() );
        recyclerListaConversas.setLayoutManager( layoutManager );
        recyclerListaConversas.setHasFixedSize( true );
        recyclerListaConversas.setAdapter( adapter );

        //adicionar evento de clique
        recyclerListaConversas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(), recyclerListaConversas, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        List<Conversa> listaConversasAtualizada = adapter.getConversas();
                        Conversa conversaSelecionada = listaConversasAtualizada.get( position );

                        if(conversaSelecionada.getIsGroup().equals("true")){
                            Intent i = new Intent(getActivity(), ChatActivity.class);
                            i.putExtra("chatGrupo", conversaSelecionada.getGrupo() );
                            startActivity( i );
                        }else{
                            Intent i = new Intent(getActivity(), ChatActivity.class);
                            i.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao() );
                            startActivity( i );
                        }


                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
                ));



        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        conversasRef = database.child("conversas").child(identificadorUsuario);


        return view;
    }

    @Override
    public void onStart() {
        recuperarConversas();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener(childEventListenerConversas);
    }
    public void pesquisarConversas(String texto){
        ArrayList<Conversa> listaConversaBusca = new ArrayList<>();
        for(Conversa conversa : listaConversas){
            if(conversa.getUsuarioExibicao() !=null){
                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
                String ultimaMensagem = conversa.getUltimaMensagem().toLowerCase();
                if(nome.contains(texto) || ultimaMensagem.contains(texto)){
                    listaConversaBusca.add(conversa);
                }
            }else{
                String nome = conversa.getGrupo().getNome().toLowerCase();
                String ultimaMensagem = conversa.getUltimaMensagem().toLowerCase();
                if(nome.contains(texto) || ultimaMensagem.contains(texto)){
                    listaConversaBusca.add(conversa);
                }
            }

        }
        adapter = new ConversasAdapter(listaConversaBusca,getActivity());
        recyclerListaConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    public void recarregarConversas(){
        adapter = new ConversasAdapter(listaConversas,getActivity());
        recyclerListaConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void recuperarConversas(){
        listaConversas.clear();
        childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Conversa conversa = snapshot.getValue(Conversa.class);
                listaConversas.add(conversa);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}