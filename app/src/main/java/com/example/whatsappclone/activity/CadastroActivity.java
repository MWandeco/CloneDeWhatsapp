package com.example.whatsappclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsappclone.R;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.helper.Base64Custom;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText editNome,editSenha,editEmail;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editNome = findViewById(R.id.editNome);
        editSenha = findViewById(R.id.editSenha);
        editEmail = findViewById(R.id.editEmail);


    }



    public void validarCadastroUsuario(View view){
        String textoNome,textoEmail,textoSenha;

        textoNome = editNome.getText().toString();
        textoEmail = editEmail.getText().toString();
        textoSenha = editSenha.getText().toString();

        if(!textoNome.isEmpty()){
            if(!textoEmail.isEmpty()){
                if(!textoSenha.isEmpty()){
                    Usuario usuario = new Usuario();
                    usuario.setNome(textoNome);
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);

                    cadastrarUsuario(usuario);

                }else{
                    Toast.makeText(CadastroActivity.this, "Digite Uma Senha", Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(CadastroActivity.this, "Digite Seu Email!!", Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(CadastroActivity.this, "Digite Seu Nome!!", Toast.LENGTH_SHORT).show();
        }


    }
    public void cadastrarUsuario(Usuario usuario){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(),usuario.getSenha())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(CadastroActivity.this,
                                    "Cadastro realizado com Sucesso",
                                    Toast.LENGTH_SHORT).show();
                            UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());
                            finish();

                            try{
                                String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                                usuario.setId(identificadorUsuario);
                                usuario.salvar();
                            }catch (Exception e){
                                e.printStackTrace();
                            }


                        }else{
                            String exception = "";

                            try {
                                throw task.getException();
                            }catch (FirebaseAuthWeakPasswordException e){
                                exception = "Digite uma senha mais forte.";
                            }catch (FirebaseAuthInvalidCredentialsException e){
                                exception = "Digite um e-mail válido.";
                            }catch (FirebaseAuthUserCollisionException e){
                                exception = "Conta já cadastrada.";
                            }catch (Exception e){
                                exception = "Erro ao cadastrar "+ e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(CadastroActivity.this
                                    , exception
                                    , Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

}