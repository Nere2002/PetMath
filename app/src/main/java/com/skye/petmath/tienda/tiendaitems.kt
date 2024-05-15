package com.skye.petmath.tienda

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="DatosTienda")
data class tiendaitems(
    @ColumnInfo(name = "tipoArticulo")
    val tipoArticulo: String,
    @ColumnInfo(name = "nombre")
    val nombrearticulo: String,
    @ColumnInfo(name="precio")
    val precio: Int,
    @ColumnInfo(name="imarticulo")
    val imarticulo: Int

){
    @PrimaryKey(autoGenerate = true)
    var Id: Int?= null
}
