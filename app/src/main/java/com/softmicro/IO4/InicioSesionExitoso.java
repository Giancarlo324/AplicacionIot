package com.softmicro.IO4;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.lang.reflect.Array;
import java.util.ArrayList;

import dmax.dialog.SpotsDialog;

public class InicioSesionExitoso extends AppCompatActivity{

    private static final String TAG = "";
    //Firebase
    FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    //Base de datos firebase.
    DatabaseReference mFirebaseDatabase;
    FirebaseDatabase mFirebaseInstance;
    //
    String token;
    //Alert
    android.app.AlertDialog dialog;
    //
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    ListView listaToken;
    //
    TextView mDatos;
    private String correo, uid, DisplayName, phone, photo;
    private androidx.appcompat.widget.Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion_exitoso);

        toolbar = (androidx.appcompat.widget.Toolbar)findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        // Detectar usuario actual.
        mAuth = FirebaseAuth.getInstance();
        // Configuración de Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //Base de datos
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("Usuarios");
        uid = mAuth.getCurrentUser().getUid();
        //Alert
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Cargando...")
                .build();

        //
        mDatos = (TextView)findViewById(R.id.txt_datos);
        listaToken = (ListView)findViewById(R.id.list_token);

        correo = mAuth.getCurrentUser().getEmail();

        //token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        dialog.show();
                        token = task.getResult().getToken();
                        registrarToken(token);

                        // Log and toast
                        Toast.makeText(InicioSesionExitoso.this, token,
                                Toast.LENGTH_SHORT).show();
                    }
                });

        mDatos.setText("Correo:" + correo + "\n UID: " + uid);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList);
        listenToken();
    }

    private void registrarToken(String token) {
        //Aquí agrego al usuario
        addUser(correo, token);
        listaToken.setAdapter(adapter);
        mFirebaseDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String add = dataSnapshot.child("Token").getValue().toString();
                arrayList.add(add);

                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String delete = dataSnapshot.getValue().toString();
                arrayList.remove(delete);

                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //Escuchador para cada token
    private void listenToken()
    {
        listaToken.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(InicioSesionExitoso.this, "Notificacion se envía a: ." + arrayList.get(position),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Función para agregar usuario.
    public void addUser(String email, String token)
    {
        User users = new User(email, token);
        System.out.println("imprimir: "+ users.Token + " : " + users.Email);
        mFirebaseDatabase.child(uid).setValue(users);
    }

    //Base
    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                        dialog.dismiss();
                    }
                });
        dialog.dismiss();
    }

    //Para detectar cuando pulsa hacia atrás.
    @Override
    public void onBackPressed() {
        AlertDialog.Builder mensaje = new AlertDialog.Builder(this);
        mensaje.setTitle("¿Seguro que quieres salir?");
        mensaje.setCancelable(true);
        mensaje.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        mensaje.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        mensaje.show();
    }
    //Toolbar
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId())
        {
            case R.id.bntCerrarses:
                signOut();
                break;
        }

        return true;
    }


    private void updateUI(FirebaseUser user) {
        if(user==null)
        {
            Toast.makeText(InicioSesionExitoso.this, "Sesión Cerrada.",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(InicioSesionExitoso.this, MainActivity.class));
            finish();
        }
    }
}
