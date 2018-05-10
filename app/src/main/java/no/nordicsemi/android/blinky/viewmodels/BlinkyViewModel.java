/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.blinky.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.blinky.R;
import no.nordicsemi.android.blinky.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.blinky.profile.BlinkyManager;
import no.nordicsemi.android.blinky.profile.BlinkyManagerCallbacks;
import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

public class BlinkyViewModel extends AndroidViewModel implements BlinkyManagerCallbacks {
	private  final  String TAG="BlinkyViewModel";
	private final BlinkyManager mBlinkyManager;

	// Connection states Connecting, Connected, Disconnecting, Disconnected etc.
	private final MutableLiveData<String> mConnectionState = new MutableLiveData<>();

	// Flag to determine if the device is connected
	private final MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();

	// Flag to determine if the device is ready
	private final MutableLiveData<Void> mOnDeviceReady = new MutableLiveData<>();
	// Flag that holds the on off state of the pump. On is turn on, Off is turn off
	private final MutableLiveData<Boolean> mPUMPState = new MutableLiveData<>();
	// Flag that holds the on off state of the pump. On is turn on, Off is turn off
	private final MutableLiveData<Boolean> mSLOWState = new MutableLiveData<>();
	// Flag that holds the on off state of the pump. On is turn on, Off is turn off
	private final MutableLiveData<Boolean> mFASTState = new MutableLiveData<>();
	// Flag that holds the on off state of the pump. On is turn on, Off is turn off
	private final MutableLiveData<Boolean> mPowerState = new MutableLiveData<>();

	public LiveData<Void> isDeviceReady() {
		return mOnDeviceReady;
	}

	public LiveData<String> getConnectionState() {
		return mConnectionState;
	}

	public LiveData<Boolean> isConnected() {
		return mIsConnected;
	}

	public LiveData<Boolean> getSLOWState() {
		return mSLOWState;
	}

	public LiveData<Boolean> getFASTState() {
		return mFASTState;
	}

	public LiveData<Boolean> getPUMPState() {
		return mPUMPState;
	}

	public LiveData<Boolean> getPowerState() {
		return mPowerState;
	}

	public BlinkyViewModel(@NonNull final Application application) {
		super(application);

		// Initialize the manager
		mBlinkyManager = new BlinkyManager(getApplication());
		mBlinkyManager.setGattCallbacks(this);
	}

	/**
	 * Connect to peripheral
	 */
	public void connect(final ExtendedBluetoothDevice device) {
		final LogSession logSession = Logger.newSession(getApplication(), null, device.getAddress(), device.getName());
		mBlinkyManager.setLogger(logSession);
		mBlinkyManager.connect(device.getDevice());
	}

	/**
	 * Disconnect from peripheral
	 */
	private void disconnect() {
		mBlinkyManager.disconnect();
	}


	/*
	 *	send cmd to device for pump turn on or off
	 */
	public void togglePump(final boolean onOff) {
		int icmd =0;
		Resources res = getApplication().getResources();
		int b=R.integer.start_fastturn;
		Log.v(TAG,"b="+b);
		if (onOff==true)	//send turn on cmd to cooler
		{
			icmd = res.getInteger(R.integer.start_pump);
			Log.v(TAG,"ipcmd="+icmd);
			Log.v(TAG,"cmd="+onOff);
		}
		else				// send turn of to cooler
		{
			icmd = res.getInteger(R.integer.stop_pump);
			Log.v(TAG,"ipcmd="+icmd);
			Log.v(TAG,"cmd="+onOff);
		}
		// send cmd via BLE to device
		mBlinkyManager.sendCMDtoThermostat(icmd);
	}
	/*
	 *	send cmd to device for Fast turn on or off
	 */
	public void toggleFast(final boolean onOff) {
		int icmd =0;
		Resources res = getApplication().getResources();

		if (onOff==true)	//send turn on cmd to cooler
		{
			icmd = res.getInteger(R.integer.start_fastturn);
			Log.v(TAG,"ipcmd="+icmd);
			Log.v(TAG,"cmd="+onOff);
		}
		else				// send turn of to cooler
		{
			icmd = res.getInteger(R.integer.stop_fastturn);
			Log.v(TAG,"ipcmd="+icmd);
			Log.v(TAG,"cmd="+onOff);
		}
		// send cmd via BLE to device
		mBlinkyManager.sendCMDtoThermostat(icmd);
		Log.v(TAG,"fast trigger");
	}
	/*
	 *	send cmd to device for Slow turn on or off
	 */
	public void toggleSlow(final boolean onOff) {
		int icmd =0;
		Resources res = getApplication().getResources();

		if (onOff==true)	//send turn on cmd to cooler
		{
			icmd = res.getInteger(R.integer.start_slowturn);
			Log.v(TAG,"ipcmd="+icmd);
			Log.v(TAG,"cmd="+onOff);
		}
		else				// send turn of to cooler
		{
			icmd = res.getInteger(R.integer.stop_slowturn);
			Log.v(TAG,"ipcmd="+icmd);
			Log.v(TAG,"cmd="+onOff);
		}
		// send cmd via BLE to device
		mBlinkyManager.sendCMDtoThermostat(icmd);
	}
	/*
	 *	send cmd to turn off device totally
	 */
	public void clickonPowerBut()
	{
		int icmd = getApplication().getResources().getInteger(R.integer.turnofdevice);
		// send cmd via BLE to device
		mBlinkyManager.sendCMDtoThermostat(icmd);
	}
	/*
	 *	check the bit is set
	 *	@param: n show the number of bit ro test
	 * 	@param: ret if bit is set it rerturn true else false
	 *   @Note: use for byte vars
	 */
	public boolean isBitSet(final int data,final int n)
	{
		if (((data & 0xff) & (1L<<n)) != 0)
			return true;
		else
			return false;
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		if (mBlinkyManager.isConnected()) {
			disconnect();
		}
	}

	@Override
	public void onDataReceived(final boolean state) {

	}

	@Override
	public void onDataSent(final boolean state) {

	}

	@Override
	public void onDeviceConnecting(final BluetoothDevice device) {
		mConnectionState.postValue(getApplication().getString(R.string.state_connecting));
	}

	@Override
	public void onDeviceConnected(final BluetoothDevice device) {
		mIsConnected.postValue(true);
		mConnectionState.postValue(getApplication().getString(R.string.state_discovering_services));
	}

	@Override
	public void onDeviceDisconnecting(final BluetoothDevice device) {
		mIsConnected.postValue(false);
	}

	@Override
	public void onDeviceDisconnected(final BluetoothDevice device) {
		mIsConnected.postValue(false);
	}

	@Override
	public void onLinklossOccur(final BluetoothDevice device) {
		mIsConnected.postValue(false);
	}

	@Override
	public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
		mConnectionState.postValue(getApplication().getString(R.string.state_initializing));
	}

	@Override
	public void onDeviceReady(final BluetoothDevice device) {
		mConnectionState.postValue(getApplication().getString(R.string.state_discovering_services_completed, device.getName()));
		mOnDeviceReady.postValue(null);
	}

	@Override
	public boolean shouldEnableBatteryLevelNotifications(final BluetoothDevice device) {
		// Blinky doesn't have Battery Service
		return false;
	}

	@Override
	public void onBatteryValueReceived(final BluetoothDevice device, final int value) {
		// Blinky doesn't have Battery Service
	}

	@Override
	public void onBondingRequired(final BluetoothDevice device) {
		// Blinky does not require bonding
	}

	@Override
	public void onBonded(final BluetoothDevice device) {
		// Blinky does not require bonding
	}

	@Override
	public void onError(final BluetoothDevice device, final String message, final int errorCode) {
		// TODO implement
	}

	@Override
	public void onDeviceNotSupported(final BluetoothDevice device) {
		// TODO implement
	}

	/*
	 *   This function handle onwritecharterictic callback to update view
	 *   if comand of turn on fast  send then view of slow should turn of and vice versa
	 *   @para: data: data of last write to FF03 characteristic
	 */
	@Override
	public void onHandleCMDtoFF03(int data)
	{
		// if cmd to turn on fast turn is received then turn of slow
		if (data==getApplication().getResources().getInteger(R.integer.start_fastturn))
		{
			mSLOWState.postValue(false);
			mPowerState.postValue(true);
		}
		// if cmd to turn on slow turn is received then turn of high
		if (data==getApplication().getResources().getInteger(R.integer.start_slowturn))
		{
			mFASTState.postValue(false);
			mPowerState.postValue(true);
		}
		// if cmd to turn on slow turn is received then turn of high
		if (data==getApplication().getResources().getInteger(R.integer.start_pump))
		{
			mPowerState.postValue(true);
		}
		// if cmd to turn on slow turn is received then turn of high
		if (data==getApplication().getResources().getInteger(R.integer.turnofdevice))
		{
			// update livedata (turning of all pump turn icon.
			mFASTState.postValue(false);
			mSLOWState.postValue(false);
			mPUMPState.postValue(false);
			mPowerState.postValue(false);
		}
	}
	/*
	 *	This Function update UI according to status sending from peripheral
	 *	@param: status according to SRS it will update UI.
	 */
	@Override
	public void onHandleGetStatus(int status)
	{
		Log.v(TAG,"status"+status+isBitSet(status,0)+isBitSet(status,1)+isBitSet(status,7));
		if (isBitSet(status,0)) //fast turn is on
		{mFASTState.postValue(true);}
		else
		{mFASTState.postValue(false);}

		if (isBitSet(status,1)) //slow turn is on
		{mSLOWState.postValue(true);}
		else
		{mSLOWState.postValue(false);}

		if (isBitSet(status,2)) //Pump turn is on
		{mPUMPState.postValue(true);}
		else
		{mPUMPState.postValue(false);}

		if (isBitSet(status,3)) //temperature turn is on
		{}
		else
		{}
		if (isBitSet(status,4)) //timeCtr turn is on
		{}
		else
		{}
		if (isBitSet(status,7)) //power turn is on
		{mPowerState.postValue(true);}
		else
		{mPowerState.postValue(false);}

	}
}
