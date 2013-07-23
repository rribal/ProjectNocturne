package com.projectnocturne;

import java.util.ArrayList;
import java.util.Timer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * This service runs in the background and will poll the TI sensor pad (bed
 * sensor) and store it's current value
 * 
 * @author andy aspell-clark
 */
public final class NocturneService extends Service {

	private Context context;

	/**
	 * Tag used on log messages.
	 */
	private static final String LOG_TAG = NocturneService.class.getSimpleName();

	private final Timer timer = new Timer();
	private String emailToUse = "unknown";

	private ArrayList<String> getGoogleAccounts() {
		final AccountManager am = AccountManager.get(getApplicationContext());
		final Account[] accounts = am.getAccounts();
		final ArrayList<String> googleAccounts = new ArrayList<String>();
		for (final Account ac : accounts) {
			final String acname = ac.name;
			final String actype = ac.type;
			// add only google accounts
			if (ac.type.equals("com.google")) {
				googleAccounts.add(ac.name);
			}
			Log.d(LOG_TAG, "accountInfo: " + acname + ":" + actype);
		}
		return googleAccounts;
	}

	@Override
	public IBinder onBind(final Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {

		final ArrayList<String> accountList = getGoogleAccounts();
		if (accountList.size() > 0) {
			emailToUse = accountList.get(0);
		}

		return super.onStartCommand(intent, flags, startId);
	}

}
