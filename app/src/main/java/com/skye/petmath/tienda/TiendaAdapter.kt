package com.skye.petmath.tienda

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skye.petmath.R
import com.skye.petmath.fragments.TiendaFragment


class TiendaAdapter(
    private val tiendaList: List<tiendaitems>,
    private val fragment: TiendaFragment,
    private val context: Context) :
    RecyclerView.Adapter<TiendaAdapter.RecompensaViewHolder>() {

    class RecompensaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenImageView: ImageView = itemView.findViewById(R.id.imarticulo)
        val nombreTextView: TextView = itemView.findViewById(R.id.nombreacrticulo)
        val botonComprar: Button = itemView.findViewById(R.id.buttontienda)
        val precio: TextView = itemView.findViewById(R.id.precio)
        var isBloqueado: Boolean = false // Campo para indicar si el botón está bloqueado
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecompensaViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_tienda, parent, false)
        return RecompensaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecompensaViewHolder, position: Int) {
        val tienda = tiendaList[position]
        holder.imagenImageView.setImageResource(tienda.imarticulo)
        holder.nombreTextView.text = tienda.nombrearticulo
        holder.precio.text = tienda.precio.toString()

//        holder.botonComprar.setOnClickListener {
//            val tipoProducto = tienda.tipoArticulo
//            Log.d("TiendaAdapter", "Botón comprar presionado para el producto: $tipoProducto")
//            fragment.comprarProducto(tienda, tienda.precio, tipoProducto)
//        }
        holder.botonComprar.setOnClickListener {
            val tipoProducto = tienda.tipoArticulo
            Log.d("TiendaAdapter", "Botón comprar presionado para el producto: $tipoProducto")

            if (!holder.isBloqueado) { // Verificar si el botón está bloqueado
                holder.isBloqueado = true // Bloquear el botón

                // Llamar a la función de compra en el fragmento
                fragment.comprarProducto(tienda, tienda.precio, tipoProducto)

                // Desbloquear el botón después de un breve retraso (por ejemplo, 1 segundo)
                Handler(Looper.getMainLooper()).postDelayed({
                    holder.isBloqueado = false
                }, 1000)
            }
        }


    }

    override fun getItemCount(): Int {
        return tiendaList.size
    }
}
