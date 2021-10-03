package com.example.whatsappclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsappclone.R;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editLoginEmail,editLoginSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editLoginEmail = findViewById(R.id.edtLoginEmail);
        editLoginSenha = findViewById(R.id.edtLoginSenha);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    }

    public void abrirTelaCadastro(View view){
        Intent intent = new Intent(LoginActivity.this,CadastroActivity.class);
        startActivity(intent);
    }

    public void logar(View view){
        String email,senha;

        email = editLoginEmail.getText().toString();
        senha = editLoginSenha.getText().toString();

            if(!email.isEmpty()){
                if(!senha.isEmpty()){
                    Usuario usuario = new Usuario();
                    usuario.setEmail(email);
                    usuario.setSenha(senha);

                    logarUsuario(usuario);

                }else{
                    Toast.makeText(LoginActivity.this, "Preencha sua Senha", Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(LoginActivity.this, "Preencha seu Email!!", Toast.LENGTH_SHORT).show();
            }

        }
        public void logarUsuario(Usuario usuario){
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                           redirecionar();
                        }else{
                            String exception = "";

                            try {
                                throw task.getException();
                            }catch (FirebaseAuthInvalidUserException e){
                                exception = "Usuário não cadastrado.";
                            }catch (FirebaseAuthInvalidCredentialsException e) {
                                exception = "E-mail ou senha inválidos.";
                            }catch (Exception e){
                                exception = "Erro ao realizar login: "+ e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(LoginActivity.this
                                    , exception
                                    , Toast.LENGTH_SHORT).show();

                        }
                    }
                });
        }
        public void redirecionar(){
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
        }

        @Override
        protected void onStart(){
            FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
            if(usuarioAtual != null){
                redirecionar();
            }
        super.onStart();
        }



    }