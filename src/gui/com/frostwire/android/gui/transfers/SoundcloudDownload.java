package com.frostwire.android.gui.transfers;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.Librarian;
import com.frostwire.android.gui.search.SoundcloudEngineSearchResult;
import com.frostwire.android.gui.util.SystemUtils;
import com.frostwire.android.util.FileUtils;

public class SoundcloudDownload implements DownloadTransfer {

    private static final String TAG = "FW.SoundcloudDownload";

    private final TransferManager manager;
    private SoundcloudEngineSearchResult sr;
    private HttpDownload delegate;

    public SoundcloudDownload(TransferManager manager, SoundcloudEngineSearchResult sr) {
        this.manager = manager;
        this.sr = sr;
    }

    @Override
    public String getDisplayName() {
        return sr.getDisplayName();
    }

    @Override
    public String getStatus() {
        return delegate != null ? delegate.getStatus() : "";
    }

    @Override
    public int getProgress() {
        return delegate != null ? delegate.getProgress() : 0;
    }

    @Override
    public long getSize() {
        return delegate != null ? delegate.getSize() : 0;
    }

    @Override
    public Date getDateCreated() {
        return delegate != null ? delegate.getDateCreated() : new Date();
    }

    @Override
    public long getBytesReceived() {
        return delegate != null ? delegate.getBytesReceived() : 0;
    }

    @Override
    public long getBytesSent() {
        return delegate != null ? delegate.getBytesSent() : 0;
    }

    @Override
    public long getDownloadSpeed() {
        return delegate != null ? delegate.getDownloadSpeed() : 0;
    }

    @Override
    public long getUploadSpeed() {
        return delegate != null ? delegate.getUploadSpeed() : 0;
    }

    @Override
    public long getETA() {
        return delegate != null ? delegate.getETA() : 0;
    }

    @Override
    public boolean isComplete() {
        return delegate != null ? delegate.isComplete() : false;
    }

    @Override
    public List<? extends TransferItem> getItems() {
        return Collections.emptyList();
    }

    @Override
    public void cancel() {
        if (delegate != null) {
            delegate.cancel();
        }
        manager.remove(this);
    }

    @Override
    public File getSavePath() {
        return delegate != null ? delegate.getSavePath() : null;
    }

    @Override
    public boolean isDownloading() {
        return delegate != null ? delegate.isDownloading() : false;
    }

    @Override
    public void cancel(boolean deleteData) {
        if (delegate != null) {
            delegate.cancel(deleteData);
        }
        manager.remove(this);
    }

    public void start() {
        try {
            final HttpDownloadLink link = buildDownloadLink();
            if (link != null) {
                delegate = new HttpDownload(manager, SystemUtils.getTempDirectory(), link);
                delegate.setListener(new HttpDownloadListener() {
                    @Override
                    public void onComplete(HttpDownload download) {
                        moveFile(download.getSavePath());
                    }
                });
                delegate.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting youtube download", e);
        }
    }

    private void moveFile(File savePath) {
        File path = SystemUtils.getSaveDirectory(Constants.FILE_TYPE_AUDIO);
        File finalFile = new File(path, savePath.getName());
        savePath.renameTo(finalFile);
        Librarian.instance().scan(finalFile);
    }

    private HttpDownloadLink buildDownloadLink() throws Exception {
        HttpDownloadLink link = new HttpDownloadLink(sr.getStreamUrl());

        link.setSize(sr.getSize());
        link.setFileName(FileUtils.getValidFileName(sr.getFileName()));
        link.setDisplayName(sr.getDisplayName());
        link.setCompressed(false);

        return link;
    }
}