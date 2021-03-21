package com.example.c_tracker

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.core.ResponseDeserializable

// 国土地理院 逆ジオコーディングAPI（公式サイトない？）
data class AddressResults(val muniCd: String, val lv01Nm: String)
data class Address(val results: AddressResults)

object AddressDeserializer : ResponseDeserializable<Address> {
    override fun deserialize(content: String): Address = jacksonObjectMapper().readValue<Address>(content, Address::class.java)
}

// 国土地理院 都道府県内市区町村一覧取得API レスポンス定義
// https://www.land.mlit.go.jp/webland/api.html#todofukenlist
data class AddressListData(val id: String, val name: String)
data class AddressList(val status: String, val data: List<AddressListData>)

object AddressListDeserializer : ResponseDeserializable<AddressList> {
    override fun deserialize(content: String): AddressList = jacksonObjectMapper().readValue<AddressList>(content, AddressList::class.java)
}
