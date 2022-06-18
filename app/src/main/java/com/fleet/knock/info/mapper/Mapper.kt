package com.fleet.knock.info.mapper

import com.google.firebase.firestore.QueryDocumentSnapshot

interface Mapper<T> {
    open fun mapping(s: QueryDocumentSnapshot) : T?
}