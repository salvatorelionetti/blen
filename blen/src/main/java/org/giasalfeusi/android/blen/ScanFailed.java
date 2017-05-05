package org.giasalfeusi.android.blen;

/**
 * Created by salvy on 12/04/17.
 */

public class ScanFailed
{
    private final int errorCode;

    public ScanFailed(int ec)
    {
        errorCode = ec;
    }

    public int getErrorCode()
    {
        return errorCode;
    }
}
