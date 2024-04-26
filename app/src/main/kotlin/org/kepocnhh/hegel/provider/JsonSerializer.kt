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

    private fun <T : Any> JSONObject.toDescribed(transform: (JSONObject) -> T): Described<T> {
        return Described(
            id = UUID.fromString(getString("id")),
            info = getJSONObject("info").toInfo(),
            item = transform(getJSONObject("item")),
        )
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
        values: (V) -> JSONObject,
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
            val value = getJSONObject(key)
            map[keys(key)] = values(value)
        }
        return map
    }

    override val remote: Serializer.Remote = object : Serializer.Remote {
        override val syncRequest: Transformer<ItemsSyncRequest> = object : Transformer<ItemsSyncRequest> {
            override fun encode(value: ItemsSyncRequest): ByteArray {
                return JSONObject()
                    .put("storageId", value.storageId.toString())
                    .put("hash", value.hash)
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsSyncRequest {
                val obj = JSONObject(String(bytes))
                return ItemsSyncRequest(
                    storageId = UUID.fromString(obj.getString("storageId")),
                    hash = obj.getString("hash"),
                )
            }
        }

        override val needUpdate: Transformer<ItemsSyncResponse.NeedUpdate> = object : Transformer<ItemsSyncResponse.NeedUpdate> {
            override fun encode(value: ItemsSyncResponse.NeedUpdate): ByteArray {
                return JSONObject()
                    .put("sessionId", value.sessionId.toString())
                    .put("info", value.info.toJSONObject(keys = UUID::toString, values = { it.toJSONObject() }))
                    .put("deleted", value.deleted.strings { it.toString() })
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsSyncResponse.NeedUpdate {
                val obj = JSONObject(String(bytes))
                return ItemsSyncResponse.NeedUpdate(
                    sessionId = UUID.fromString(obj.getString("sessionId")),
                    info = obj.getJSONObject("info").toMap(keys = UUID::fromString, values = { it.toInfo() }),
                    deleted = obj.getJSONArray("deleted").strings(UUID::fromString).toSet(),
                )
            }
        }

        override val syncMerge: Transformer<ItemsSyncMergeRequest> = object : Transformer<ItemsSyncMergeRequest> {
            override fun encode(value: ItemsSyncMergeRequest): ByteArray {
                return JSONObject()
                    .put("download", value.download.strings { it.toString() })
                    .put("items", JSONArray(String(foo.list.encode(value.items))))
                    .put("deleted", value.deleted.strings { it.toString() })
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsSyncMergeRequest {
                val obj = JSONObject(String(bytes))
                return ItemsSyncMergeRequest(
                    download = obj.getJSONArray("download").strings(UUID::fromString).toSet(),
                    items = foo.list.decode(obj.getJSONArray("items").toString().toByteArray()),
                    deleted = obj.getJSONArray("deleted").strings(UUID::fromString).toSet(),
                )
            }
        }

        override val mergeResponse: Transformer<ItemsSyncMergeResponse> = object : Transformer<ItemsSyncMergeResponse> {
            override fun encode(value: ItemsSyncMergeResponse): ByteArray {
                return JSONObject()
                    .put("items", JSONArray(String(foo.list.encode(value.items))))
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsSyncMergeResponse {
                val obj = JSONObject(String(bytes))
                return ItemsSyncMergeResponse(
                    items = foo.list.decode(obj.getJSONArray("items").toString().toByteArray()),
                )
            }
        }
    }
}
