package org.kepocnhh.hegel.entity

import android.os.Parcel
import android.os.Parcelable

internal class FileDelegateParcelable(
    val delegate: FileDelegate,
) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(delegate.hash.size)
        dest.writeByteArray(delegate.hash)
        dest.writeLong(delegate.size)
    }

    override fun toString(): String {
        return delegate.toString()
    }

    override fun hashCode(): Int {
        return delegate.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is FileDelegate -> delegate == other
            is FileDelegateParcelable -> delegate == other.delegate
            else -> false
        }
    }

    companion object CREATOR : Parcelable.Creator<FileDelegateParcelable> {
        override fun createFromParcel(parcel: Parcel): FileDelegateParcelable {
            val hash = ByteArray(parcel.readInt())
            parcel.readByteArray(hash)
            val size = parcel.readLong()
            return FileDelegateParcelable(
                delegate = FileDelegate(
                    hash = hash,
                    size = size,
                ),
            )
        }

        override fun newArray(size: Int): Array<FileDelegateParcelable?> {
            return arrayOfNulls(size)
        }
    }
}
