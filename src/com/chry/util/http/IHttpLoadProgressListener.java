package com.chry.util.http;

public abstract interface IHttpLoadProgressListener {
    public abstract void loadStart();

    public abstract void loadFinished(LoadEvent e);

    public abstract void progress(int paramInt);
}
