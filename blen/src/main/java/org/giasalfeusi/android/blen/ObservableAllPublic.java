package org.giasalfeusi.android.blen;

import android.util.Log;

import java.util.Observable;

/**
 * Created by salvy on 13/03/17.
 */

/* To let to operate on Observable object with inheriting from it */
class ObservableAllPublic extends Observable {
    final static private String TAG = "ObservableAP";

    public void clearChanged()
    {
        super.clearChanged();
    }
    public void setChanged()
    {
        super.setChanged();
    }
}
