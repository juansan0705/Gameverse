package com.jsancre.gameverse.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jsancre.gameverse.R;
import com.jsancre.gameverse.models.SliderItem;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase se utiliza como adaptador para proporcionar los datos y la vista de los elementos en un SliderView,
 * que es un componente que muestra una serie de elementos deslizantes.
 */
public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH> {

    private Context context;
    private List<SliderItem> mSliderItems = new ArrayList<>();

    public SliderAdapter(Context context, List<SliderItem> sliderItems) {
        this.context = context;
        mSliderItems = sliderItems;
    }

    //Se implementa el método onCreateViewHolder() para crear y devolver una instancia de la clase interna
    // SliderAdapterVH, que extiende de SliderViewAdapter.ViewHolder
    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_layout_item, null);
        return new SliderAdapterVH(inflate);
    }

    //Se implementa el método onBindViewHolder() para actualizar la vista del elemento del slider en
    // función de los datos en la posición dada.
    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {
        SliderItem sliderItem = mSliderItems.get(position);
        if (sliderItem.getImageUrl() != null){
            if (!sliderItem.getImageUrl().isEmpty()){
                Picasso.with(context).load(sliderItem.getImageUrl()).into(viewHolder.imageViewSlider);
            }
        }
    }

    //Se implementa el método getCount() para devolver el número total de elementos en la lista mSliderItems
    @Override
    public int getCount() {
        return mSliderItems.size();
    }

    //La clase interna SliderAdapterVH se utiliza para mantener las referencias a los elementos
    // de la vista del elemento del slider, en este caso, el imageViewSlider
    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewSlider;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewSlider = itemView.findViewById(R.id.imageViewSlider);

            this.itemView = itemView;
        }
    }

}