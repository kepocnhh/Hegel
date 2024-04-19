package org.kepocnhh.hegel.provider

import org.json.JSONArray
import org.json.JSONObject
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Meta
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

internal class JsonSerializer : Serializer {
    private val _meta: Transformer<Meta> = object : Transformer<Meta> {
        override fun encode(value: Meta): ByteArray {
            return JSONObject()
                .put("id", value.id.toString())
                .put("updated", value.updated.inWholeMilliseconds)
                .put("hash", value.hash)
                .toString()
                .toByteArray()
        }

        override fun decode(bytes: ByteArray): Meta {
            val obj = JSONObject(String(bytes))
            return Meta(
                id = UUID.fromString(obj.getString("id")),
                updated = obj.getLong("updated").milliseconds,
                hash = obj.getString("hash"),
            )
        }
    }

    override val meta: ListTransformer<Meta> = object : ListTransformer<Meta> {
        override fun encode(value: Meta): ByteArray {
            return _meta.encode(value)
        }

        override val list: Transformer<List<Meta>> = object : Transformer<List<Meta>> {
            override fun encode(value: List<Meta>): ByteArray {
                val array = JSONArray()
                for (it in value) {
                    array.put(JSONObject(String(_meta.encode(it)))) // todo
                }
                return array.toString().toByteArray()
            }

            override fun decode(bytes: ByteArray): List<Meta> {
                val array = JSONArray(String(bytes))
                return (0 until array.length()).map { index ->
                    val obj = array.getJSONObject(index)
                    _meta.decode(obj.toString().toByteArray())
                }
            }
        }

        override fun decode(bytes: ByteArray): Meta {
            return _meta.decode(bytes)
        }
    }

    private val _uuid: Transformer<UUID> = object : Transformer<UUID> {
        override fun encode(value: UUID): ByteArray {
            return value.toString().toByteArray()
        }

        override fun decode(bytes: ByteArray): UUID {
            return UUID.fromString(String(bytes))
        }
    }

    override val uuid: ListTransformer<UUID> = object : ListTransformer<UUID> {
        override fun encode(value: UUID): ByteArray {
            return _uuid.encode(value)
        }

        override val list: Transformer<List<UUID>> = object : Transformer<List<UUID>> {
            override fun encode(value: List<UUID>): ByteArray {
                val array = JSONArray()
                for (it in value) {
                    array.put(String(_uuid.encode(it))) // todo
                }
                return array.toString().toByteArray()
            }

            override fun decode(bytes: ByteArray): List<UUID> {
                val array = JSONArray(String(bytes))
                return (0 until array.length()).map { index ->
                    _uuid.decode(array.getString(index).toByteArray())
                }
            }
        }

        override fun decode(bytes: ByteArray): UUID {
            return _uuid.decode(bytes)
        }
    }

    private val _foo: Transformer<Foo> = object : Transformer<Foo> {
        override fun encode(value: Foo): ByteArray {
            return JSONObject()
                .put("id", value.id.toString())
                .put("created", value.created.inWholeMilliseconds)
                .put("text", value.text)
                .toString()
                .toByteArray()
        }

        override fun decode(bytes: ByteArray): Foo {
            val obj = JSONObject(String(bytes))
            return Foo(
                id = UUID.fromString(obj.getString("id")),
                created = obj.getLong("created").milliseconds,
                text = obj.getString("text"),
            )
        }
    }

    override val foo: ListTransformer<Foo> = object : ListTransformer<Foo> {
        override fun encode(value: Foo): ByteArray {
            return _foo.encode(value)
        }

        override val list: Transformer<List<Foo>> = object : Transformer<List<Foo>> {
            override fun encode(value: List<Foo>): ByteArray {
                val array = JSONArray()
                for (it in value) {
                    array.put(JSONObject(String(_foo.encode(it)))) // todo
                }
                return array.toString().toByteArray()
            }

            override fun decode(bytes: ByteArray): List<Foo> {
                val array = JSONArray(String(bytes))
                return (0 until array.length()).map { index ->
                    val obj = array.getJSONObject(index)
                    _foo.decode(obj.toString().toByteArray())
                }
            }
        }

        override fun decode(bytes: ByteArray): Foo {
            return _foo.decode(bytes)
        }
    }

    override val remote: Serializer.Remote = object : Serializer.Remote {
        override val syncRequest: Transformer<ItemsSyncRequest> = object : Transformer<ItemsSyncRequest> {
            override fun encode(value: ItemsSyncRequest): ByteArray {
                return JSONObject()
                    .put("id", value.id.toString())
                    .put("hash", value.hash)
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsSyncRequest {
                val obj = JSONObject(String(bytes))
                return ItemsSyncRequest(
                    id = UUID.fromString(obj.getString("id")),
                    hash = obj.getString("hash"),
                )
            }
        }

        override val needUpdate: Transformer<ItemsSyncResponse.NeedUpdate> = object : Transformer<ItemsSyncResponse.NeedUpdate> {
            override fun encode(value: ItemsSyncResponse.NeedUpdate): ByteArray {
                return JSONObject()
                    .put("sessionId", value.sessionId.toString())
                    .put("metas", JSONArray(String(meta.list.encode(value.metas))))
                    .put("deleted", JSONArray(String(uuid.list.encode(value.deleted))))
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsSyncResponse.NeedUpdate {
                val obj = JSONObject(String(bytes))
                return ItemsSyncResponse.NeedUpdate(
                    sessionId = UUID.fromString(obj.getString("sessionId")),
                    metas = meta.list.decode(obj.getJSONArray("metas").toString().toByteArray()),
                    deleted = uuid.list.decode(obj.getJSONArray("deleted").toString().toByteArray()),
                )
            }
        }

        override val syncMerge: Transformer<ItemsSyncMergeRequest> = object : Transformer<ItemsSyncMergeRequest> {
            override fun encode(value: ItemsSyncMergeRequest): ByteArray {
                return JSONObject()
                    .put("download", JSONArray(String(uuid.list.encode(value.download))))
                    .put("items", JSONArray(String(foo.list.encode(value.items))))
                    .put("deleted", JSONArray(String(uuid.list.encode(value.deleted))))
                    .toString()
                    .toByteArray()
            }

            override fun decode(bytes: ByteArray): ItemsSyncMergeRequest {
                val obj = JSONObject(String(bytes))
                return ItemsSyncMergeRequest(
                    download = uuid.list.decode(obj.getJSONArray("download").toString().toByteArray()),
                    items = foo.list.decode(obj.getJSONArray("items").toString().toByteArray()),
                    deleted = uuid.list.decode(obj.getJSONArray("deleted").toString().toByteArray()),
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
