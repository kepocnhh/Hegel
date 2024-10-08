package org.kepocnhh.hegel.provider

import org.json.JSONArray
import org.json.JSONObject
import org.kepocnhh.hegel.entity.Bar
import org.kepocnhh.hegel.entity.FileDelegate
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsMergeRequest
import org.kepocnhh.hegel.entity.ItemsMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Pic
import sp.kx.storages.CommitInfo
import sp.kx.storages.HashFunction
import sp.kx.storages.HashesTransformer
import sp.kx.storages.ItemInfo
import sp.kx.storages.MergeInfo
import sp.kx.storages.Metadata
import sp.kx.storages.Payload
import sp.kx.storages.RawPayload
import sp.kx.storages.SyncInfo
import sp.kx.storages.SyncResponseTransformer
import sp.kx.storages.SyncSession
import sp.kx.storages.Transformer
import java.util.Base64
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

internal class JsonSerializer(
    private val hf: HashFunction,
) : Serializer {
    private fun Metadata.toJSONObject(): JSONObject {
        return JSONObject()
            .put("id", id.toString())
            .put("created", created.inWholeMilliseconds)
            .put("info", info.toJSONObject())
    }

    private fun JSONObject.toMetadata(): Metadata {
        return Metadata(
            id = UUID.fromString(getString("id")),
            created = getLong("created").milliseconds,
            info = getJSONObject("info").toItemInfo(),
        )
    }

    private fun ItemInfo.toJSONObject(): JSONObject {
        return JSONObject()
            .put("hash", hash.base64())
            .put("updated", updated.inWholeMilliseconds)
    }

    private fun JSONObject.toItemInfo(): ItemInfo {
        return ItemInfo(
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

    private fun <T : Any> Payload<T>.toJSONObject(transform: (T) -> JSONObject): JSONObject {
        return JSONObject()
            .put("meta", meta.toJSONObject())
            .put("value", transform(value))
    }

    private fun ByteArray.base64(): String {
        return Base64.getEncoder().encodeToString(this)
    }

    private fun String.base64(): ByteArray {
        return Base64.getDecoder().decode(this)
    }

    private fun RawPayload.toJSONObject(): JSONObject {
        return JSONObject()
            .put("meta", meta.toJSONObject())
            .put("bytes", bytes.base64())
    }

    private fun <T : Any> JSONObject.toPayload(transform: (JSONObject) -> T): Payload<T> {
        return Payload(
            meta = getJSONObject("meta").toMetadata(),
            value = transform(getJSONObject("value")),
        )
    }

    private fun JSONObject.toDescribed(): RawPayload {
        return RawPayload(
            meta = getJSONObject("meta").toMetadata(),
            bytes = getString("bytes").base64(),
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
            .put("downloaded", downloaded.strings { it.toString() })
            .put("items", items.objects { it.toJSONObject() })
    }

    private fun JSONObject.toMergeInfo(): MergeInfo {
        return MergeInfo(
            deleted = getJSONArray("deleted").strings(UUID::fromString).toSet(),
            downloaded = getJSONArray("downloaded").strings(UUID::fromString).toSet(),
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
        private val hashesTransformer = HashesTransformer(hf = hf)
        private val syncResponseDelegate = SyncResponseTransformer(hf = hf)
        override val syncRequest: Transformer<ItemsSyncRequest> = object : Transformer<ItemsSyncRequest> {
            override fun encode(decoded: ItemsSyncRequest): ByteArray {
                return hashesTransformer.encode(decoded = decoded.hashes)
            }

            override fun decode(encoded: ByteArray): ItemsSyncRequest {
                return ItemsSyncRequest(
                    hashes = hashesTransformer.decode(encoded = encoded),
                )
            }
        }

        override val syncResponse: Transformer<ItemsSyncResponse> = object : Transformer<ItemsSyncResponse> {
            override fun encode(decoded: ItemsSyncResponse): ByteArray {
                return JSONObject()
                    .put("sessionId", decoded.sessionId.toString())
                    .put("delegate", syncResponseDelegate.encode(decoded.delegate).base64())
                    .toString()
                    .toByteArray()
            }

            override fun decode(encoded: ByteArray): ItemsSyncResponse {
                val obj = JSONObject(String(encoded))
                return ItemsSyncResponse(
                    sessionId = UUID.fromString(obj.getString("sessionId")),
                    delegate = syncResponseDelegate.decode(obj.getString("delegate").base64()),
                )
            }
        }

        private fun SyncSession.toJSONObject(): JSONObject {
            return JSONObject()
                .put("dst", dst.base64())
                .put("src", src.base64())
        }

        private fun JSONObject.toSyncSession(): SyncSession {
            return SyncSession(
                dst = getString("dst").base64(),
                src = getString("src").base64(),
            )
        }

        override val mergeRequest: Transformer<ItemsMergeRequest> = object : Transformer<ItemsMergeRequest> {
            override fun encode(decoded: ItemsMergeRequest): ByteArray {
                val merges = decoded.merges.toJSONObject(
                    keys = UUID::toString,
                    values = { it.toJSONObject() },
                )
                return JSONObject()
                    .put("sessionId", decoded.sessionId.toString())
                    .put("merges", merges)
                    .put("syncSession", decoded.syncSession.toJSONObject())
                    .toString()
                    .toByteArray()
            }

            override fun decode(encoded: ByteArray): ItemsMergeRequest {
                val obj = JSONObject(String(encoded))
                val merges = obj
                    .getJSONObject("merges")
                    .toMap(
                        keys = UUID::fromString,
                        values = { it.toMergeInfo() },
                    )
                return ItemsMergeRequest(
                    sessionId = UUID.fromString(obj.getString("sessionId")),
                    merges = merges,
                    syncSession = obj.getJSONObject("syncSession").toSyncSession(),
                )
            }
        }

        override val mergeResponse: Transformer<ItemsMergeResponse> = object : Transformer<ItemsMergeResponse> {
            override fun encode(decoded: ItemsMergeResponse): ByteArray {
                val commits = decoded.commits.toJSONObject(
                    keys = UUID::toString,
                    values = { it.toJSONObject() },
                )
                return JSONObject()
                    .put("commits", commits)
                    .toString()
                    .toByteArray()
            }

            override fun decode(encoded: ByteArray): ItemsMergeResponse {
                val obj = JSONObject(String(encoded))
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
        override fun encode(decoded: Foo): ByteArray {
            return decoded.toJSONObject().toString().toByteArray()
        }

        override fun decode(encoded: ByteArray): Foo {
            return JSONObject(String(encoded)).toFoo()
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
        override fun encode(decoded: Bar): ByteArray {
            return decoded.toJSONObject().toString().toByteArray()
        }

        override fun decode(encoded: ByteArray): Bar {
            return JSONObject(String(encoded)).toBar()
        }
    }

    private fun Pic.toJSONObject(): JSONObject {
        return JSONObject()
            .put("title", title)
            .put("fd", fd?.toJSONObject())
    }

    private fun JSONObject.toPic(): Pic {
        return Pic(
            title = getString("title"),
            fd = if (has("fd")) getJSONObject("fd").toFileDelegate() else null,
        )
    }

    override val pics = object : Transformer<Pic> {
        override fun decode(encoded: ByteArray): Pic {
            return JSONObject(String(encoded)).toPic()
        }

        override fun encode(decoded: Pic): ByteArray {
            return decoded.toJSONObject().toString().toByteArray()
        }
    }

    private fun FileDelegate.toJSONObject(): JSONObject {
        return JSONObject()
            .put("hash", hash.base64())
            .put("size", size)
    }

    private fun JSONObject.toFileDelegate(): FileDelegate {
        return FileDelegate(
            hash = getString("hash").base64(),
            size = getInt("size"),
        )
    }

    override val fds = object : Transformer<FileDelegate> {
        override fun decode(encoded: ByteArray): FileDelegate {
            return JSONObject(String(encoded)).toFileDelegate()
        }

        override fun encode(decoded: FileDelegate): ByteArray {
            return decoded.toJSONObject().toString().toByteArray()
        }
    }
}
