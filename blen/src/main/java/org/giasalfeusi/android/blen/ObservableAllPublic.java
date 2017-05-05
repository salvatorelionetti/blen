package org.giasalfeusi.android.blen;

import java.util.Observable;

/**
 * Created by salvy on 13/03/17.
 */

/* To let to operate on Observable object with inheriting from it */
/* TODO Add an 2 phase notification
 * 1) Notify, no state is changed (e.g. validate an action and schedule the execution)
 * 2) Execute state change (e.g. connect)
 */
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
