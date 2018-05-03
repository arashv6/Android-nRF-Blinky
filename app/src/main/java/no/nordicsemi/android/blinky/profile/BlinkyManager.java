/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.blinky.profile;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.log.LogContract;

public class BlinkyManager extends BleManager<BlinkyManagerCallbacks> {
    private final String TAG="BlinkyManager";
    /**
     * Nordic Blinky Service UUID
     */
    public final static UUID LBS_UUID_SERVICE = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    /*
     * Mask for UUID
     */
    public final static UUID LBS_UUID_SERVICE_MASK = UUID.fromString("0000ff00-0000-0000-0000-00000000000");
    /**
     * BUTTON characteristic UUID
     */
    private final static UUID GATT_COMMANDCHAHANDLE = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
    /**
     * LED characteristic UUID
     */
    private final static UUID GATT_CONDITIONCHAHANDLE = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic mConditionChaHandle, mCMDCharacteristic;

    public BlinkyManager(final Context context) {
        super(context);
    }

    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    @Override
    protected boolean shouldAutoConnect() {
        // If you want to connect to the device using autoConnect flag = true, return true here.
        // Read the documentation of this method.
        return super.shouldAutoConnect();
    }

    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
     */
    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

        @Override
        protected Deque<Request> initGatt(final BluetoothGatt gatt) {
            final LinkedList<Request> requests = new LinkedList<>();
            requests.push(Request.newReadRequest(mCMDCharacteristic));
            requests.push(Request.newReadRequest(mConditionChaHandle));
            requests.push(Request.newEnableNotificationsRequest(mConditionChaHandle));
            return requests;
        }

        @Override
        public boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(LBS_UUID_SERVICE);
            if (service != null) {
                mConditionChaHandle = service.getCharacteristic(GATT_CONDITIONCHAHANDLE);
                mCMDCharacteristic = service.getCharacteristic(GATT_COMMANDCHAHANDLE);
            }
/*
			boolean writeRequest = false;
			if (mCMDCharacteristic != null) {
				final int rxProperties = mCMDCharacteristic.getProperties();
				writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
			}
*/
            //return mConditionChaHandle != null && mCMDCharacteristic != null && writeRequest;
            return mCMDCharacteristic != null;
        }

        @Override
        protected void onDeviceDisconnected() {
            mConditionChaHandle = null;
            mCMDCharacteristic = null;
        }

        @Override
        protected void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            final int data = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            if (characteristic == mCMDCharacteristic) {
                log(LogContract.Log.Level.APPLICATION, "mCMDCharacteristic has been read");
                Log.v(TAG,"mCMDCharacteristic:"+data);
                //mCallbacks.onDataSent(ledOn);
            }
            else if(characteristic == mConditionChaHandle){
                log(LogContract.Log.Level.APPLICATION, "mConditionChaHandle has ben read");
                Log.v(TAG,"mConditionChaHandle:"+data);
            }
            Log.v(TAG,"Callback of Read");
        }

        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // This method is only called for LED characteristic
            final int data = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            if (characteristic == mCMDCharacteristic)
            {
                final boolean ledOn = (data == 0x01);
                log(LogContract.Log.Level.APPLICATION, "LED " + (ledOn ? "ON" : "OFF"));
                mCallbacks.onDataSent(ledOn);
                mCallbacks.onHandleCMDtoFF03(data);
            }
            Log.v(TAG,"Callback of write");
        }

        @Override
        public void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // This method is only called for Button characteristic
            int data = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            if (characteristic == mConditionChaHandle)
            {
                log(LogContract.Log.Level.APPLICATION, "Status Recieved" + data);
                mCallbacks.onHandleGetStatus(data);
                /* Only for debug */
                //Log.v(TAG,"notify data="+(data & 0xFF));
                /*****************/
            }
            Log.v(TAG,"Callback of Notification");
        }
    };

    public void send(final boolean onOff) {
/*		// Are we connected?
		if (mCMDCharacteristic == null)
			return;
		*//* only for test *//*
		boolean i=readCharacteristic(mConditionChaHandle);
		Log.v(TAG,"readCond="+i);
        int val= mConditionChaHandle.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0);
        Log.v(TAG,"val read="+val);
        byte[] val2= mConditionChaHandle.getValue();
        Log.v(TAG,"val read array="+val2[0]);
        *//*****************//*
		final byte[] command = new byte[] {(byte) (onOff ? 1 : 0)};
		mCMDCharacteristic.setValue(command);
		log(LogContract.Log.Level.WARNING, "Turning LED " + (onOff ? "ON" : "OFF") + "...");
		writeCharacteristic(mCMDCharacteristic);*/
    }

    /*
     *   This function send cmd to pheripheral device
     *   it will send to FF03 GATT characteristic.
     *   @para: cmd : command type to device
     */
    public void sendCMDtoThermostat(int cmd)
    {
        final byte[] command= new byte[] {(byte) cmd};
        mCMDCharacteristic.setValue(command);
        Log.v(TAG,"send cmd="+ command);
        writeCharacteristic(mCMDCharacteristic);
    }


}
