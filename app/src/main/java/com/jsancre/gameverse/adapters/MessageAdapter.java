package com.jsancre.gameverse.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jsancre.gameverse.R;
import com.jsancre.gameverse.activities.ChatActivity;
import com.jsancre.gameverse.models.Message;
import com.jsancre.gameverse.providers.AuthProvider;
import com.jsancre.gameverse.providers.UsersProvider;
import com.jsancre.gameverse.utils.RelativeTime;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
//RECOMENTAR!

/**
 * Esta clase adapta los comentarios de los usuarios a la pantalla de 'Post Detail Activity'
 */
public class MessageAdapter extends FirestoreRecyclerAdapter<Message, MessageAdapter.ViewHolder> {

    //---------------------
    //     ATRIBUTOS
    //---------------------
    Context context;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;

    //---------------------
    //     CONSTRUCTOR
    //---------------------
    public MessageAdapter(FirestoreRecyclerOptions<Message> options, Context context) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
    }

    //---------------------
    //     MÉTODOS
    //---------------------

    //Con este metodo cogemos la plantilla del 'cardView_comment' y la llamamos cada vez que se comenta en una publicación
    // Y le insertamos los datos de ese post
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Message message) throws IndexOutOfBoundsException {
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String messageId = document.getId();
        holder.textViewMessage.setText(message.getMessage());
        String relativeTime = RelativeTime.timeFormatAMPM(message.getTimestamp(), context);
        holder.textViewDateMessage.setText(relativeTime);
        //Aqui validamos qien esta enviando el mensaje para que en la vista se adapte de una forma u otra
        if (message.getIdSender().equals(mAuthProvider.getUid())){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(150,0,0,0);
            holder.mlinearLayoutMessage.setLayoutParams(params);
            holder.mlinearLayoutMessage.setPadding(30,20,0,20);
            holder.mlinearLayoutMessage.setBackground(context.getResources().getDrawable(R.drawable.rounded_linear_layout));
            holder.imageViewViewedMessage.setVisibility(View.VISIBLE);
            holder.textViewMessage.setTextColor(Color.WHITE);
            holder.textViewDateMessage.setTextColor(Color.LTGRAY);
        }
        //Aqui establecemos el formato de mensaje de el usuario que esta mandandolo
        else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(0,0,150,0);
            holder.mlinearLayoutMessage.setLayoutParams(params);
            holder.mlinearLayoutMessage.setPadding(30,20,30,20);
            holder.mlinearLayoutMessage.setBackground(context.getResources().getDrawable(R.drawable.rounded_linear_layout_grey));
            holder.imageViewViewedMessage.setVisibility(View.GONE);
            holder.textViewMessage.setTextColor(Color.DKGRAY);
            holder.textViewDateMessage.setTextColor(Color.LTGRAY);
        }

        //Aqui cambiamos el icono de visto cuando el metodo tiene el estado del mensaje como leido.
        if (message.isViewed()){
            holder.imageViewViewedMessage.setImageResource(R.drawable.check_blue_light);
        } else {
            holder.imageViewViewedMessage.setImageResource(R.drawable.check_grey);
        }

    }





    // Este metodo que estoy sobrescribiendo crea y devuelve una instancia de ViewHolder para una vista de comentario en un RecyclerView,
    // que se utilizará para mantener referencias a los elementos de la vista y facilitar el acceso y la actualización
    // de los datos del comentario.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_message, parent, false);
        return new ViewHolder(view);
    }

    // la clase ViewHolder se utiliza para mantener referencias a los elementos de la vista de un comentario en un RecyclerView,
    // lo cual permite acceder y actualizar fácilmente los datos de ese comentario.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;
        TextView textViewDateMessage;
        ImageView imageViewViewedMessage;
        LinearLayout mlinearLayoutMessage;
        View viewHolder;

        public ViewHolder(View view){
            super(view);
            textViewMessage = view.findViewById(R.id.textViewMessage);
            textViewDateMessage = view.findViewById(R.id.textViewDateMessage);
            imageViewViewedMessage = view.findViewById(R.id.imageViewViewedMessage);
            mlinearLayoutMessage = view.findViewById(R.id.linearLayoutMessage);
            viewHolder = view;
        }
    }
}
