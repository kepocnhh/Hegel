package org.kepocnhh.hegel.provider

import org.json.JSONArray
import org.json.JSONObject
import org.kepocnhh.hegel.entity.Bar
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsMergeRequest
import org.kepocnhh.hegel.entity.ItemsMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.util.Hashes
import sp.kx.storages.CommitInfo
import sp.kx.storages.Described
import sp.kx.storages.HashFunction
import java.util.Base64
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import sp.kx.storages.ItemInfo
import sp.kx.storages.MergeInfo
import sp.kx.storages.SyncInfo
import sp.kx.storages.Transformer

internal class JsonSerializer(
    private val hf: HashFunction,
) : Serializer {
    private fun ItemInfo.toJSONObject(): JSONObject {
        return JSONObject()
            .put("hash", hash.base64())
            .put("created", created.inWholeMilliseconds)
            .put("updated", updated.inWholeMilliseconds)
    }

    private fun JSONObject.toItemInfo(): ItemInfo {
        return ItemInfo(
            created = getLong("created").milliseconds,
            updated = getLong("updated").milliseconds,
            hash = getString("hash").base64(),
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
            info = getJSONObject("info").toItemInfo(),
            item = transform(getJSONObject("item")),
        )
    }

    private fun JSONObject.toDescribed(): Described<ByteArray> {
        return Described(
            id = UUID.fromString(getString("id")),
            info = getJSONObject("info").toItemInfo(),
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

    private fun <K : Any> JSONObject.fromBase64(
        keys: (String) -> K,
    ): Map<K, ByteArray> {
        val map = mutableMapOf<K, ByteArray>()
        for (key in keys()) {
            map[keys(key)] = getString(key).base64()
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

    private fun <K : Any> Map<K, ByteArray>.toBase64(
        keys: (K) -> String,
    ): JSONObject {
        val obj = JSONObject()
        for ((key, value) in this) {
            obj.put(keys(key), value.base64())
        }
        return obj
    }

    private fun SyncInfo.toJSONObject(): JSONObject {
        return JSONObject()
            .put("deleted", deleted.strings { it.toString() })
            .put("infos", infos.toJSONObject(keys = UUID::toString, values = { it.toJSONObject() }))
    }

    private fun JSONObject.toStorageInfo(): SyncInfo {
        return SyncInfo(
            deleted = getJSONArray("deleted").strings(UUID::fromString).toSet(),
            infos = getJSONObject("infos").toMap(keys = UUID::fromString, values = { it.toItemInfo() }),
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

    private fun CommitInfo.toJSONObject(): JSONObject {
        return JSONObject()
            .put("hash", hash.base64())
            .put("deleted", deleted.strings { it.toString() })
            .put("items", items.objects { it.toJSONObject() })
    }

    private fun JSONObject.toCommitInfo(): CommitInfo {
        return CommitInfo(
            hash = getString("hash").base64(),
            deleted = getJSONArray("deleted").strings(UUID::fromString).toSet(),
            items = getJSONArray("items").objects { it.toDescribed() },
        )
    }

    override val remote: Serializer.Remote = object : Serializer.Remote {
        override val syncRequest: Transformer<ItemsSyncRequest> = object : Transformer<ItemsSyncRequest> {
            override fun encode(decoded: ItemsSyncRequest): ByteArray {
                return Hashes.toByteArray(hashes = decoded.hashes, hf = hf)
            }

            override fun decode(encoded: ByteArray): ItemsSyncRequest {
                return ItemsSyncRequest(
                    hashes = Hashes.fromByteArray(encoded = encoded, hf = hf),
                )
            }
        }

        override val syncResponse: Transformer<ItemsSyncResponse> = object : Transformer<ItemsSyncResponse> {
            override fun encode(value: ItemsSyncResponse): ByteArray {
                return JSONObject()
                    .put("sessionId", value.sessionId.toString())
                    .put("syncs", value.syncs.toJSONObject(keys = UUID::toString, values = { it.toJSONObject() }))
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsSyncResponse {
                val obj = JSONObject(String(bytes))
                return ItemsSyncResponse(
                    sessionId = UUID.fromString(obj.getString("sessionId")),
                    syncs = obj.getJSONObject("syncs").toMap(keys = UUID::fromString, values = { it.toStorageInfo() }),
                )
            }
        }

        override val mergeRequest: Transformer<ItemsMergeRequest> = object : Transformer<ItemsMergeRequest> {
            override fun encode(value: ItemsMergeRequest): ByteArray {
                val merges = value.merges.toJSONObject(
                    keys = UUID::toString,
                    values = { it.toJSONObject() },
                )
                return JSONObject()
                    .put("sessionId", value.sessionId.toString())
                    .put("merges", merges)
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsMergeRequest {
                val obj = JSONObject(String(bytes))
                val merges = obj
                    .getJSONObject("merges")
                    .toMap(
                        keys = UUID::fromString,
                        values = { it.toMergeInfo() },
                    )
                return ItemsMergeRequest(
                    sessionId = UUID.fromString(obj.getString("sessionId")),
                    merges = merges,
                )
            }
        }

        override val mergeResponse: Transformer<ItemsMergeResponse> = object : Transformer<ItemsMergeResponse> {
            override fun encode(value: ItemsMergeResponse): ByteArray {
                val commits = value.commits.toJSONObject(
                    keys = UUID::toString,
                    values = { it.toJSONObject() },
                )
                return JSONObject()
                    .put("commits", commits)
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsMergeResponse {
                val obj = JSONObject(String(bytes))
                val commits = obj.getJSONObject("commits").toMap(
                    keys = UUID::fromString,
                    values = { it.toCommitInfo() },
                )
                return ItemsMergeResponse(
                    commits = commits,
                )
            }
        }
    }

    override val foo = object : Transformer<Foo> {
        override fun encode(value: Foo): ByteArray {
            return value.toJSONObject().toString().toByteArray()
        }

        override fun decode(bytes: ByteArray): Foo {
            return JSONObject(String(bytes)).toFoo()
        }
    }

    private fun Bar.toJSONObject(): JSONObject {
        return JSONObject()
            .put("count", count)
    }

    private fun JSONObject.toBar(): Bar {
        return Bar(
            count = getInt("count"),
        )
    }

    override val bar = object : Transformer<Bar> {
        override fun encode(value: Bar): ByteArray {
            return value.toJSONObject().toString().toByteArray()
        }

        override fun decode(bytes: ByteArray): Bar {
            return JSONObject(String(bytes)).toBar()
        }
    }
}
