package org.giasalfeusi.android.blen;

import java.util.Observer;

/**
 * Created by salvy on 12/04/17.
 */

public interface ObserverWithFilter extends Observer
{
    /* TODO ADD A STRUCT TO MAP CLASS => CALLBACK
     * Or magic things with overloading and introspecting
     * (i.e. defining multiple update(ClassA), ...)
     */
    public Class[] getObservedClass();
}
