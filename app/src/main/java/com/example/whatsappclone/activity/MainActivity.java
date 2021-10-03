package com.example.whatsappclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.whatsappclone.R;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.fragment.ContatosFragment;
import com.example.whatsappclone.fragment.ConversasFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = ConfiguracaoFirebase.getFirebaseAutenticacao();


        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        FragmentPagerItemAdapter fpAdapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.conversas, ConversasFragment.class)
                .add(R.string.contatos, ContatosFragment.class)
                .create()
        );

        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(fpAdapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);

        //Configuração do searchview
        searchView = findViewById(R.id.materialSearchPrincipal);
        //listener searchview
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                ConversasFragment fragment = (ConversasFragment) fpAdapter.getPage(0);
                fragment.recarregarConversas();
            }
        });
        //mecanismo de busca searchview
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Log.d("QUERY","onQuerytextoSubmit");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
               // Log.d("QUERY","onQuerytextoChange");

                //verifica se o usuário está pesquisando conversas ou contatos
                switch (viewPager.getCurrentItem()){
                    case 0 :
                        ConversasFragment conversasFragment = (ConversasFragment) fpAdapter.getPage(0);
                        if(newText != null && !newText.isEmpty()){
                            conversasFragment.pesquisarConversas(newText.toLowerCase());
                        }else{
                            conversasFragment.recarregarConversas();
                        }
                        break;
                    case 1:
                        ContatosFragment contatosFragment = (ContatosFragment) fpAdapter.getPage(0);
                        if(newText != null && !newText.isEmpty()){
                            contatosFragment.pesquisarContatos(newText.toLowerCase());
                        }else{
                            contatosFragment.recarregarContatos();
                        }
                        break;
                }


                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main,menu);
        MenuItem item = menu.findItem(R.id.pesquisa);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.sair:
                deslogarUsuario();
                finish();
                break;
            case R.id.configuracoes:
                abrirConfiguracoes();
                break;
            case R.id.pesquisa:
                searchView.setMenuItem(item);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario(){
        try{
            auth.signOut();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void abrirConfiguracoes(){
        Intent intent = new Intent(MainActivity.this,ConfiguracoesActivity.class);
        startActivity(intent);
    }


}