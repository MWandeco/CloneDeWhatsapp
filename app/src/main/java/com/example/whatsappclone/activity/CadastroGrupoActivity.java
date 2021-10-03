package com.example.whatsappclone.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.R;
import com.example.whatsappclone.adapter.GrupoSelecionadoAdapter;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Grupo;
import com.example.whatsappclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {

    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private TextView textTotalParticipantes;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private RecyclerView recyclerMembrosGrupo;
    private CircleImageView imageGrupo;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private Grupo grupo;
    private FloatingActionButton fabSalvarGrupo;
    private EditText editNomeGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);
        //configurações iniciais
        textTotalParticipantes = findViewById(R.id.textTotalParticipantes);
        recyclerMembrosGrupo = findViewById(R.id.recyclerMembrosGrupo);
        imageGrupo = findViewById(R.id.imageGrupo);
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        grupo = new Grupo();
        fabSalvarGrupo = findViewById(R.id.floatingActionButtonSalvarGrupo);
        editNomeGrupo = findViewById(R.id.editNomeGrupo);



        // Salvar foto do grupo
        imageGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (i.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(i, SELECAO_GALERIA);
            }
        });

        if (getIntent().getExtras() != null) {
            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("Membros");
            listaMembrosSelecionados.addAll(membros);
            textTotalParticipantes.setText("Participantes : " + listaMembrosSelecionados.size());
        }
        //configurar do recyclerview
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(), LinearLayoutManager.HORIZONTAL, false
        );
        recyclerMembrosGrupo.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosGrupo.setHasFixedSize(true);
        recyclerMembrosGrupo.setAdapter(grupoSelecionadoAdapter);

        //configurar floating action button
        fabSalvarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeGrupo = editNomeGrupo.getText().toString();

                //adiciona á lista de membros o usuario que está logado
                listaMembrosSelecionados.add(UsuarioFirebase.getDadosUsuarioLogado());
                grupo.setMembros(listaMembrosSelecionados);

                grupo.setNome(nomeGrupo);
                grupo.salvar();

                Intent i = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                i.putExtra("chatGrupo", grupo );
                startActivity( i );
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;
            try {
                Uri localDaImagem = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localDaImagem);
                if (imagem != null) {
                    imageGrupo.setImageBitmap(imagem);
                    //recuperar os dados para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                    byte[] dadosDaImagem = baos.toByteArray();

                    //salvar imagem no firebase
                    final StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("grupos")
                            .child(grupo.getId() + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosDaImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "Download Falhou!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(),
                                    "Download realizado com Sucesso!",
                                    Toast.LENGTH_SHORT).show();

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String url = task.getResult().toString();
                                    grupo.setFoto(url);
                                }
                            });

                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}