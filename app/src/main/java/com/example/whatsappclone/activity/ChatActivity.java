package com.example.whatsappclone.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.adapter.MensagensAdapter;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.helper.Base64Custom;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Conversa;
import com.example.whatsappclone.model.Grupo;
import com.example.whatsappclone.model.Mensagem;
import com.example.whatsappclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private EditText editMensagem;
    private Usuario usuarioDestinatario;
    private DatabaseReference database;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;
    private ImageView imageCamera;
    private static final int SELECAO_CAMERA = 100;
    private StorageReference storage;
    private Grupo grupo;

    //identificador usuarios remetente e destinatario
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;
    private Usuario usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();

    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configuracoes iniciais
        textViewNome = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera = findViewById(R.id.imageCamera);
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        //recupera dados do usuario remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();

        //Recuperar dados do usuário destinatario
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("chatGrupo")) {
                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                textViewNome.setText(grupo.getNome());

                String foto = grupo.getFoto();
                if (foto != null) {
                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleImageViewFoto);
                } else {
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

            } else {
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textViewNome.setText(usuarioDestinatario.getNome());

                String foto = usuarioDestinatario.getFoto();
                if (foto != null) {
                    Uri url = Uri.parse(usuarioDestinatario.getFoto());
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleImageViewFoto);
                } else {
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }
                //recuperar dados usuario destinatario
                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
            }
        }

        //Configuração adapter
        adapter = new MensagensAdapter(mensagens, getApplicationContext());

        //Configuração recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize(true);
        recyclerMensagens.setAdapter(adapter);

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (i.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(i, SELECAO_CAMERA);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;

            try {
                switch (requestCode) {
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                }
                if (imagem != null) {
                    //recuperar os dados para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                    byte[] dadosDaImagem = baos.toByteArray();

                    //criar nome da imagem
                    String nomeImagem = UUID.randomUUID().toString();

                    //configurar referencias para o firebase
                    final StorageReference imagensRef = storage
                            .child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem + ".jpeg");

                    UploadTask uploadTask = imagensRef.putBytes(dadosDaImagem);
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
                            imagensRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String downloadUrl = task.getResult().toString();

                                    Mensagem mensagem = new Mensagem();
                                    mensagem.setIdUsuario(idUsuarioRemetente);
                                    mensagem.setMensagem("imagem.jpeg");
                                    mensagem.setImagem(downloadUrl);

                                    //salvar imagem para o remetente
                                    salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);
                                    //salvar imagem para o destinatario
                                    salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

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

    public void enviarMensagem(View view) {

        String textoMensagem = editMensagem.getText().toString();

        if (!textoMensagem.isEmpty()) {
            if (usuarioDestinatario != null) {
                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);

                //Salvar mensagem para o remetente
                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                //salvar mensagem para o destinatario
                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                //salvar conversa
                salvarConversa(idUsuarioRemetente,idUsuarioDestinatario,usuarioDestinatario,mensagem, false);

                //salvar conversa usuario destinatario
                salvarConversa(idUsuarioDestinatario,idUsuarioRemetente,usuarioRemetente,mensagem,false);
            } else {
                for (Usuario membro : grupo.getMembros()) {
                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                    mensagem.setMensagem(textoMensagem);
                    mensagem.setNome(usuarioRemetente.getNome());

                    //salvar mensagem no grupo
                    salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);
                    //salvar conversa
                    salvarConversa(idRemetenteGrupo,idUsuarioDestinatario,usuarioDestinatario,mensagem, true);
                }
            }
        } else {
            Toast.makeText(ChatActivity.this,
                    "Digite uma mensagem para enviar!",
                    Toast.LENGTH_LONG).show();
        }

    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg) {

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        //Limpar texto
        editMensagem.setText("");

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    private void recuperarMensagens() {
        mensagens.clear();

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void salvarConversa(String idRemetente, String idDestinatario,Usuario usuarioExibicao, Mensagem msg, boolean isGroup) {
        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());

        if (isGroup) {
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo(grupo);
        } else {
            conversaRemetente.setUsuarioExibicao(usuarioExibicao);
            conversaRemetente.setIsGroup("false");

        }
        conversaRemetente.salvar();
    }

}