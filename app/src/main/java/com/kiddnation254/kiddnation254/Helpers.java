package com.kiddnation254.kiddnation254;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Helpers {
    private Context _context;

    public Helpers(Context context){
        this._context = context;
    }

    public boolean isConnectedToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }
}
