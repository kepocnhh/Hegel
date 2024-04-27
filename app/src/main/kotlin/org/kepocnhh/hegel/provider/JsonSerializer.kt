package org.kepocnhh.hegel.provider

import org.json.JSONArray
import org.json.JSONObject
import org.kepocnhh.hegel.entity.Described
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.Info
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.MergeInfo
import org.kepocnhh.hegel.entity.StorageInfo
import java.util.Base64
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

internal class JsonSerializer : Serializer {
    private fun Info.toJSONObject(): JSONObject {
        return JSONObject()
            .put("hash", hash)
            .put("created", created.inWholeMilliseconds)
            .put("updated", updated.inWholeMilliseconds)
    }

    private fun JSONObject.toInfo(): Info {
        return Info(
            created = getLong("created").milliseconds,
            updated = getLong("updated").milliseconds,
            hash = getString("hash"),
        )
    }

    private fun Foo.toJSONObject(): JSONObject {
        return JSONObject()
            .put("text", text)
    }

    private fun JSONObject.toFoo(): Foo {
        return Foo(
            text = getString("text"),
        )
    }

    private fun <T : Any> Described<T>.toJSONObject(transform: (T) -> JSONObject): JSONObject {
        return JSONObject()
            .put("id", id.toString())
            .put("info", info.toJSONObject())
            .put("item", transform(item))
    }

    private fun ByteArray.base64(): String {
        return Base64.getEncoder().encodeToString(this)
    }

    private fun String.base64(): ByteArray {
        return Base64.getDecoder().decode(this)
    }

    private fun Described<ByteArray>.toJSONObject(): JSONObject {
        return JSONObject()
            .put("id", id.toString())
            .put("info", info.toJSONObject())
            .put("item", item.base64())
    }

    private fun <T : Any> JSONObject.toDescribed(transform: (JSONObject) -> T): Described<T> {
        return Described(
            id = UUID.fromString(getString("id")),
            info = getJSONObject("info").toInfo(),
            item = transform(getJSONObject("item")),
        )
    }

    private fun JSONObject.toDescribed(): Described<ByteArray> {
        return Described(
            id = UUID.fromString(getString("id")),
            info = getJSONObject("info").toInfo(),
            item = getString("item").base64(),
        )
    }

    private fun <T : Any> Iterable<T>.objects(transform: (T) -> JSONObject): JSONArray {
        val array = JSONArray()
        for (item in this) {
            array.put(transform(item))
        }
        return array
    }

    private fun <T : Any> JSONArray.objects(transform: (JSONObject) -> T): List<T> {
        val list = mutableListOf<T>()
        for (i in 0 until length()) {
            list += transform(getJSONObject(i))
        }
        return list
    }

    override val foo = object : ListTransformer<Described<Foo>> {
        override fun encode(value: Described<Foo>): ByteArray {
            return value.toJSONObject { it.toJSONObject() }.toString().toByteArray()
        }

        override val list = object : Transformer<List<Described<Foo>>> {
            override fun encode(value: List<Described<Foo>>): ByteArray {
                val array = JSONArray()
                for (item in value) {
                    array.put(item.toJSONObject { it.toJSONObject() })
                }
                return array.toString().toByteArray()
            }

            override fun decode(bytes: ByteArray): List<Described<Foo>> {
                val list = mutableListOf<Described<Foo>>()
                val array = JSONArray(String(bytes))
                for (i in 0 until array.length()) {
                    list += array.getJSONObject(i).toDescribed { it.toFoo() }
                }
                return list
            }
        }

        override fun decode(bytes: ByteArray): Described<Foo> {
            return JSONObject(String(bytes)).toDescribed { it.toFoo() }
        }
    }

    private fun <T : Any> Iterable<T>.strings(transform: (T) -> String): JSONArray {
        val array = JSONArray()
        for (it in this) {
            array.put(transform(it))
        }
        return array
    }

    private fun <T : Any> JSONArray.strings(transform: (String) -> T): List<T> {
        val list = mutableListOf<T>()
        for (i in 0 until length()) {
            list += transform(getString(i))
        }
        return list
    }

    private fun <K : Any, V: Any> Map<K, V>.toJSONObject(
        keys: (K) -> String,
        values: (K, V) -> JSONObject,
    ): JSONObject {
        val obj = JSONObject()
        for ((key, value) in this) {
            obj.put(keys(key), values(key, value))
        }
        return obj
    }

    private fun <K : Any, V: Any> Map<K, V>.arrays(
        keys: (K) -> String,
        values: (V) -> JSONArray,
    ): JSONObject {
        val obj = JSONObject()
        for ((key, value) in this) {
            obj.put(keys(key), values(value))
        }
        return obj
    }

    private fun <K : Any, V: Any> JSONObject.toMap(
        keys: (String) -> K,
        values: (JSONObject) -> V,
    ): Map<K, V> {
        val map = mutableMapOf<K, V>()
        for (key in keys()) {
            map[keys(key)] = values(getJSONObject(key))
        }
        return map
    }

    private fun <K : Any, V: Any> JSONObject.arrays(
        keys: (String) -> K,
        values: (JSONArray) -> V,
    ): Map<K, V> {
        val map = mutableMapOf<K, V>()
        for (key in keys()) {
            map[keys(key)] = values(getJSONArray(key))
        }
        return map
    }

    private fun <K : Any, V: Any> JSONObject.strings(
        keys: (String) -> K,
        values: (String) -> V,
    ): Map<K, V> {
        val map = mutableMapOf<K, V>()
        for (key in keys()) {
            map[keys(key)] = values(getString(key))
        }
        return map
    }

    private fun <K : Any> JSONObject.strings(
        keys: (String) -> K,
    ): Map<K, String> {
        val map = mutableMapOf<K, String>()
        for (key in keys()) {
            map[keys(key)] = getString(key)
        }
        return map
    }

    private fun <K : Any, V: Any> Map<K, V>.toStrings(
        keys: (K) -> String,
        values: (V) -> String,
    ): JSONObject {
        val obj = JSONObject()
        for ((key, value) in this) {
            obj.put(keys(key), values(value))
        }
        return obj
    }

    private fun <K : Any> Map<K, String>.toStrings(
        keys: (K) -> String,
    ): JSONObject {
        val obj = JSONObject()
        for ((key, value) in this) {
            obj.put(keys(key), value)
        }
        return obj
    }

    private fun StorageInfo.toJSONObject(): JSONObject {
        return JSONObject()
            .put("deleted", deleted.strings { it.toString() })
            .put("meta", meta.toJSONObject(keys = UUID::toString, values = { _, it -> it.toJSONObject() }))
    }

    private fun JSONObject.toStorageInfo(): StorageInfo {
        return StorageInfo(
            deleted = getJSONArray("deleted").strings(UUID::fromString).toSet(),
            meta = getJSONObject("meta").toMap(keys = UUID::fromString, values = { it.toInfo() }),
        )
    }

    private fun MergeInfo.toJSONObject(): JSONObject {
        return JSONObject()
            .put("deleted", deleted.strings { it.toString() })
            .put("download", download.strings { it.toString() })
            .put("items", items.objects { it.toJSONObject() })
    }

    private fun JSONObject.toMergeInfo(): MergeInfo {
        return MergeInfo(
            deleted = getJSONArray("deleted").strings(UUID::fromString).toSet(),
            download = getJSONArray("download").strings(UUID::fromString).toSet(),
            items = getJSONArray("items").objects { it.toDescribed() },
        )
    }

    override val remote: Serializer.Remote = object : Serializer.Remote {
        override val syncRequest: Transformer<ItemsSyncRequest> = object : Transformer<ItemsSyncRequest> {
            override fun encode(value: ItemsSyncRequest): ByteArray {
                return JSONObject()
                    .put("hashes", value.hashes.toStrings(keys = UUID::toString))
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsSyncRequest {
                val obj = JSONObject(String(bytes))
                return ItemsSyncRequest(
                    hashes = obj.getJSONObject("hashes").strings(keys = UUID::fromString),
                )
            }
        }

        override val needUpdate: Transformer<ItemsSyncResponse.NeedUpdate> = object : Transformer<ItemsSyncResponse.NeedUpdate> {
            override fun encode(value: ItemsSyncResponse.NeedUpdate): ByteArray {
                return JSONObject()
                    .put("sessionId", value.sessionId.toString())
                    .put("storages", value.storages.toJSONObject(keys = UUID::toString, values = { _, it -> it.toJSONObject() }))
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsSyncResponse.NeedUpdate {
                val obj = JSONObject(String(bytes))
                return ItemsSyncResponse.NeedUpdate(
                    sessionId = UUID.fromString(obj.getString("sessionId")),
                    storages = obj.getJSONObject("storages").toMap(keys = UUID::fromString, values = { it.toStorageInfo() }),
                )
            }
        }

        override val syncMerge: Transformer<ItemsSyncMergeRequest> = object : Transformer<ItemsSyncMergeRequest> {
            override fun encode(value: ItemsSyncMergeRequest): ByteArray {
                val storages = value.storages.toJSONObject(
                    keys = UUID::toString,
                    values = { _, it -> it.toJSONObject() },
                )
                return JSONObject()
                    .put("sessionId", value.sessionId.toString())
                    .put("storages", storages)
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsSyncMergeRequest {
                val obj = JSONObject(String(bytes))
                val storages = obj
                    .getJSONObject("storages")
                    .toMap(
                        keys = UUID::fromString,
                        values = { it.toMergeInfo() },
                    )
                return ItemsSyncMergeRequest(
                    sessionId = UUID.fromString(obj.getString("sessionId")),
                    storages = storages,
                )
            }
        }

        override val mergeResponse: Transformer<ItemsSyncMergeResponse> = object : Transformer<ItemsSyncMergeResponse> {
            override fun encode(value: ItemsSyncMergeResponse): ByteArray {
                val storages = value.storages.arrays(
                    keys = UUID::toString,
                    values = { list -> list.objects { it.toJSONObject() } },
                )
                return JSONObject()
                    .put("storages", storages)
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsSyncMergeResponse {
                val obj = JSONObject(String(bytes))
                val storages = obj.getJSONObject("storages").arrays(
                    keys = UUID::fromString,
                    values = { array -> array.objects { it.toDescribed() } },
                )
                return ItemsSyncMergeResponse(
                    storages = storages,
                )
            }
        }
    }

    override val fooItem = object : Transformer<Foo> {
        override fun encode(value: Foo): ByteArray {
            return value.toJSONObject().toString().toByteArray()
        }

        override fun decode(bytes: ByteArray): Foo {
            return JSONObject(String(bytes)).toFoo()
        }
    }
}
