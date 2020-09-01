/*
 * ownCloud Android client application
 *
 * @author David A. Velasco
 * @author David González Verdugo
 * @author Christian Schabesberger
 * @author Shashvat Kedia
 * <p>
 * Copyright (C) 2020 ownCloud GmbH.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.data.files.storage

import android.net.Uri
import android.os.Environment

object FileStorageUtils {
    /**
     * Get absolute path to tmp folder inside datafolder in sd-card for given accountName.
     */
    fun getTemporalPath(accountName: String): String {
        val sdCard = Environment.getExternalStorageDirectory()
        return "${sdCard.absolutePath}/owncloud/tmp/${Uri.encode(accountName, "@")}"
        // URL encoding is an 'easy fix' to overcome that NTFS and FAT32 don't allow ":" in file names,
        // that can be in the accountName since 0.1.190B
    }

    /**
     * Optimistic number of bytes available on sd-card.
     *
     * @return Optimistic number of available bytes (can be less)
     */
    fun getUsableSpace(): Long {
        val savePath = Environment.getExternalStorageDirectory()
        return savePath.usableSpace
    }

    /**
     * Get local path where OCFile file is to be stored after upload. That is,
     * corresponding local path (in local owncloud storage) to remote uploaded
     * file.
     */
    fun getDefaultSavePathFor(
        accountName: String,
        remotePath: String
    ): String {
        return getSavePath(accountName) + remotePath
    }

    /**
     * Get local owncloud storage path for accountName.
     */
    fun getSavePath(accountName: String): String {
        val sdCard = Environment.getExternalStorageDirectory()
        return "${sdCard.absolutePath}/owncloud/" + Uri.encode(accountName, "@")
        // URL encoding is an 'easy fix' to overcome that NTFS and FAT32 don't allow ":" in file names,
        // that can be in the accountName since 0.1.190B
    }

}
