package com.skye.petmath.tienda

import androidx.lifecycle.ViewModel
import com.skye.petmath.R

class TiendaVewModel : ViewModel() {
    val comidaList = listOf(
        tiendaitems("comida","FRUTA",12, R.drawable.comidagato ),
        tiendaitems("comida","CARNE",8, R.drawable.amburgesa ),
        tiendaitems("comida","PAELLA",23, R.drawable.paella )
    )
    val bebidaList = listOf(
        tiendaitems("bebida","AGUA",7, R.drawable.bebidagato ),
        tiendaitems("bebida","LECHE",12, R.drawable.leche1 ),
        tiendaitems("bebida","ZUMO",22, R.drawable.zumo ),
        )

    val jugeteList = listOf(
        tiendaitems("salut","MUÑECO",6, R.drawable.jugetegato ),
        tiendaitems("salut","PELOTA",12, R.drawable.pelota ),
        tiendaitems("salut","RATONJUGETE",22, R.drawable.ratonjugete ),
        )
}