package com.modulap.nidosano.data.firebase

import com.modulap.nidosano.data.model.Tip
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await


suspend fun getGuide(): List<Tip> {
    val firestore = Firebase.firestore

    val snapshot = firestore.collection("guides")
        .get()
        .await()

    println("Documentos en guides: ${snapshot.size()}")

    val guides = mutableListOf<Tip>()

    for (doc in snapshot.documents) {
        println("Doc id: ${doc.id}, data: ${doc.data}")

        val title = doc.getString("title")
        val recomendation = doc.getString("recommendation")
        val measures = doc.getString("measures")
        val type = doc.getString("type")

        if (title == null || recomendation == null || measures == null || type == null) {
            println("Documento ${doc.id} incompleto, se ignora")
            continue
        }

        guides.add(
            Tip(
                title = title,
                recomendation = recomendation,
                measures = measures,
                type = type
            )
        )
    }

    return guides
}
