package de.vyacheslav.kushchenko.staff.vpn.util.model

interface EntityConverter<K, V> {
    fun V.asModel(): K

    fun K.asEntity(): V
}
