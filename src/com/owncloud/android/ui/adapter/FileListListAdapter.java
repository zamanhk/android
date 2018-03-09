/**
 * ownCloud Android client application
 *
 * @author Bartek Przybylski
 * @author Tobias Kaminsky
 * @author David A. Velasco
 * @author masensio
 * Copyright (C) 2011  Bartek Przybylski
 * Copyright (C) 2016 ownCloud GmbH.
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
package com.owncloud.android.ui.adapter;


import java.util.ArrayList;
import java.util.Vector;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.datamodel.ThumbnailsCacheManager;
import com.owncloud.android.files.services.FileDownloader.FileDownloaderBinder;
import com.owncloud.android.files.services.FileUploader.FileUploaderBinder;
import com.owncloud.android.services.OperationsService.OperationsServiceBinder;
import com.owncloud.android.ui.activity.ComponentsGetter;
import com.owncloud.android.ui.fragment.FileFragment;
import com.owncloud.android.ui.fragment.OCFileListFragment;
import com.owncloud.android.utils.DisplayUtils;
import com.owncloud.android.utils.FileStorageUtils;
import com.owncloud.android.utils.MimetypeIconUtil;

import java.util.Vector;

/**
 * This Adapter populates a ListView with all files and folders in an ownCloud
 * instance.
 */
public class FileListListAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    private Context mContext;
    private Vector<OCFile> mFiles = null;
    private boolean mJustFolders;

    private FileDataStorageManager mStorageManager;
    private OCFileListFragment mOCFileListFragment;
    private boolean mGridMode;
    private Account mAccount;
    private ComponentsGetter mTransferServiceGetter;
    private FileFragment.ContainerActivity mContainerActivity;
    private String mFooterText;


    private static final int TYPE_LIST = 0;
    private static final int TYPE_GRID = 1;
    private static final int TYPE_GRID_IMAGE = 2;
    protected static final int TYPE_FOOTER = 3;

    private SharedPreferences mAppPreferences;

    public FileListListAdapter(
            boolean justFolders,
            Context context,
            ComponentsGetter transferServiceGetter,
            FileFragment.ContainerActivity mContainerActivity
            ) {

        this.mContainerActivity = mContainerActivity;
        mJustFolders = justFolders;
        mContext = context;
        mAccount = AccountUtils.getCurrentOwnCloudAccount(mContext);
        mAppPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);

        mTransferServiceGetter = transferServiceGetter;

        // Read sorting order, default to sort by name ascending
        FileStorageUtils.mSortOrder = com.owncloud.android.db.PreferenceManager.getSortOrder(mContext);
        FileStorageUtils.mSortAscending = com.owncloud.android.db.PreferenceManager.getSortAscending(mContext);
        
        // initialise thumbnails cache on background thread
        new ThumbnailsCacheManager.InitDiskCacheTask().execute();

        mGridMode = false;
        mFooterText = null;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    public Object getItem(int position) {
        if (mFiles == null || mFiles.size() <= position)
            return null;
        if (position <= mFiles.size()) {
            return mFiles.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        if (mFiles == null || mFiles.size() <= position)
            return 0;
        return mFiles.get(position).getFileId();
    }

    /**
     * Returns count of items + 1 to show footer
     * @return return file count + 1 to show footer
     */
    @Override
    public int getItemCount() {
        return (null != mFiles && mFiles.size() > 0 ? mFiles.size()+1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mFiles.size())
        {
            return TYPE_FOOTER;
        } else
        if (isGridMode()) {
            if (((OCFile)getItem(position)).isImage())
            {
                return TYPE_GRID_IMAGE;
            }
            return TYPE_GRID;
        } else if (!isGridMode()) {
            return TYPE_LIST;
        } else
            return super.getItemViewType(position);
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int viewLayout;
        switch (viewType)
        {
            case TYPE_LIST:
                viewLayout = R.layout.list_item;
                break;
            case TYPE_GRID:
                viewLayout = R.layout.grid_item;
                break;
            case TYPE_GRID_IMAGE:
                viewLayout = R.layout.grid_image;
                break;
            case TYPE_FOOTER:
                viewLayout = R.layout.list_footer;
                break;
            default:
                viewLayout = R.layout.list_item;
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(viewLayout, parent, false);
        return new RecyclerViewHolder(view, this, mContainerActivity, mOCFileListFragment);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {

        int viewLayout = getItemViewType(position);

        OCFile file = null;
        if (mFiles != null && mFiles.size() > position) {
            file = mFiles.get(position);
        }

        if (viewLayout == TYPE_FOOTER) // This is a footer layout, set footer text
        {
            holder.footerText.setText(getFooterText());
        } else               // This are normal items
            if (file != null) {

                if (holder.fileName != null) {
                    holder.fileName.setText(file.getFileName());
                }

                if (holder.lastModV != null) {
                    holder.lastModV.setVisibility(View.VISIBLE);
                    holder.lastModV.setText(showRelativeTimestamp(file));
                }

                if (holder.checkBoxV != null) {
                    holder.checkBoxV.setVisibility(View.GONE);
                }

                if (holder.fileSizeV != null && holder.fileSizeSeperatorV != null) {
                    if (!file.isFolder()) {
                        holder.fileSizeV.setVisibility(View.VISIBLE);
                        holder.fileSizeSeperatorV.setVisibility(View.VISIBLE);
                        holder.fileSizeV.setText(DisplayUtils.bytesToHumanReadable(file.getFileLength()));
                    }
                    else
                    {
                        holder.fileSizeV.setVisibility(View.GONE);
                        holder.fileSizeSeperatorV.setVisibility(View.GONE);
                    }
                }

                // sharedIcon
                if (file.isSharedViaLink()) {
                    holder.sharedIconV.setVisibility(View.VISIBLE);
                } else {
                    holder.sharedIconV.setVisibility(View.GONE);
                }

                // local state
                holder.localStateView.bringToFront();
                FileDownloaderBinder downloaderBinder =
                        mTransferServiceGetter.getFileDownloaderBinder();
                FileUploaderBinder uploaderBinder =
                        mTransferServiceGetter.getFileUploaderBinder();
                OperationsServiceBinder opsBinder =
                        mTransferServiceGetter.getOperationsServiceBinder();

                holder.localStateView.setVisibility(View.INVISIBLE);   // default first

                if ( //synchronizing
                        opsBinder != null && opsBinder.isSynchronizing(mAccount, file.getRemotePath())) {
                    holder.localStateView.setImageResource(R.drawable.ic_synchronizing);
                    holder.localStateView.setVisibility(View.VISIBLE);

                } else if ( // downloading
                        downloaderBinder != null && downloaderBinder.isDownloading(mAccount, file)) {
                    holder.localStateView.setImageResource(R.drawable.ic_synchronizing);
                    holder.localStateView.setVisibility(View.VISIBLE);

                } else if ( //uploading
                        uploaderBinder != null && uploaderBinder.isUploading(mAccount, file)) {
                    holder.localStateView.setImageResource(R.drawable.ic_synchronizing);
                    holder.localStateView.setVisibility(View.VISIBLE);

                } else if (file.getEtagInConflict() != null) {   // conflict
                    holder.localStateView.setImageResource(R.drawable.ic_synchronizing_error);
                    holder.localStateView.setVisibility(View.VISIBLE);

                } else if (file.isDown()) {
                    holder.localStateView.setImageResource(R.drawable.ic_synced);
                    holder.localStateView.setVisibility(View.VISIBLE);
                }

                // this if-else is needed even though favorite icon is visible by default
                // because android reuses views in listview
                if (!file.isAvailableOffline()) {
                    holder.favoriteIcon.setVisibility(View.GONE);
                } else {
                    holder.favoriteIcon.setVisibility(View.VISIBLE);
                }

                // Icons and thumbnail utils
                // TODO : image processing, this has to be fixed !!!!

                // No Folder
                if (!file.isFolder()) {
                    if (file.isImage() && file.getRemoteId() != null) {
                        // Thumbnail in Cache?
                        Bitmap thumbnail = ThumbnailsCacheManager.getBitmapFromDiskCache(
                                String.valueOf(file.getRemoteId())
                        );
                        if (thumbnail != null && !file.needsUpdateThumbnail()) {
                            holder.fileIcon.setImageBitmap(thumbnail);
                        } else {
                            // generate new Thumbnail
                            if (ThumbnailsCacheManager.cancelPotentialThumbnailWork(file, holder.fileIcon)) {
                                final ThumbnailsCacheManager.ThumbnailGenerationTask task =
                                        new ThumbnailsCacheManager.ThumbnailGenerationTask(
                                                holder.fileIcon, mStorageManager, mAccount
                                        );
                                if (thumbnail == null) {
                                    thumbnail = ThumbnailsCacheManager.mDefaultImg;
                                }
                                final ThumbnailsCacheManager.AsyncThumbnailDrawable asyncDrawable =
                                        new ThumbnailsCacheManager.AsyncThumbnailDrawable(
                                                mContext.getResources(),
                                                thumbnail,
                                                task
                                        );
                                holder.fileIcon.setImageDrawable(asyncDrawable);
                                task.execute(file);
                            }
                        }

                        if (file.getMimetype().equalsIgnoreCase("image/png")) {
                            holder.fileIcon.setBackgroundColor(mContext.getResources()
                                    .getColor(R.color.background_color));
                        }
                    } else {
                        holder.fileIcon.setImageResource(MimetypeIconUtil.getFileTypeIconId(file.getMimetype(),
                                file.getFileName()));
                    }
                } else {
                    // Folder
                    holder.fileIcon.setImageResource(
                            MimetypeIconUtil.getFolderTypeIconId(
                                    file.isSharedWithMe() || file.isSharedWithSharee(),
                                    file.isSharedViaLink()
                            )
                    );
                }
            }


    }

    /**
     * Return list of current files
     *
     * @return Vector of OCFiles
     */
    public Vector<OCFile> getCurrentFiles() {
        return mFiles;
    }

    /**
     * Change the adapted directory for a new one
     *
     * @param folder                New folder to adapt. Can be NULL, meaning
     *                              "no content to adapt".
     * @param updatedStorageManager Optional updated storage manager; used to replace
     *                              mStorageManager if is different (and not NULL)
     */
    public void swapDirectory(OCFile folder, FileDataStorageManager updatedStorageManager
            /*, boolean onlyOnDevice*/) {
        if (updatedStorageManager != null && updatedStorageManager != mStorageManager) {
            mStorageManager = updatedStorageManager;
            mAccount = AccountUtils.getCurrentOwnCloudAccount(mContext);
        }
        if (mStorageManager != null) {
            // TODO Enable when "On Device" is recovered ?
            mFiles = mStorageManager.getFolderContent(folder/*, onlyOnDevice*/);

            mFiles = mStorageManager.getFolderContent(mFile/*, onlyOnDevice*/);
            mFilesOrig.clear();
            mFilesOrig.addAll(mFiles);

            if (mJustFolders) {
                mFiles = getFolders(mFiles);
            }
        } else {
            mFiles = null;
        }

        mFiles = FileStorageUtils.sortFolder(mFiles);
        notifyDataSetChanged();
    }

    /**
     * Filter for getting only the folders
     *
     * @param files             Collection of files to filter
     * @return                  Folders in the input
     */
    public Vector<OCFile> getFolders(Vector<OCFile> files) {
        Vector<OCFile> ret = new Vector<>();
        OCFile current;
        for (int i = 0; i < files.size(); i++) {
            current = files.get(i);
            if (current.isFolder()) {
                ret.add(current);
            }
        }
        return ret;
    }

    public void setSortOrder(Integer order, boolean ascending) {

        com.owncloud.android.db.PreferenceManager.setSortOrder(order, mContext);
        com.owncloud.android.db.PreferenceManager.setSortAscending(ascending, mContext);
        
        FileStorageUtils.mSortOrder = order;
        FileStorageUtils.mSortAscending = ascending;

        mFiles = FileStorageUtils.sortFolder(mFiles);
        notifyDataSetChanged();
    }
    
    private CharSequence showRelativeTimestamp(OCFile file){
        return DisplayUtils.getRelativeDateTimeString(mContext, file.getModificationTimestamp(),
                DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
    }

    public void setGridMode(boolean gridMode) {
        mGridMode = gridMode;
    }

    public boolean isGridMode() {
        return mGridMode;
    }

    public void setFooterText(String mFooterText)
    {
        this.mFooterText = mFooterText;
    }

    private String getFooterText()
    {
        return mFooterText;
    }

    public ArrayList<OCFile> getCheckedItems(AbsListView parentList) {
        SparseBooleanArray checkedPositions = parentList.getCheckedItemPositions();
        ArrayList<OCFile> files = new ArrayList<>();
        Object item;
        for (int i=0; i < checkedPositions.size(); i++) {
            if (checkedPositions.valueAt(i)) {
                item = getItem(checkedPositions.keyAt(i));
                if (item != null) {
                    files.add((OCFile)item);
                }
            }
        }
        return files;
    }
}
