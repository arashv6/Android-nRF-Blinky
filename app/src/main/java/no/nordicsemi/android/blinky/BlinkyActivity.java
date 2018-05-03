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

package no.nordicsemi.android.blinky;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import butterknife.OnClick;
import no.nordicsemi.android.blinky.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.blinky.viewmodels.BlinkyViewModel;

public class BlinkyActivity extends AppCompatActivity {
	public static final String EXTRA_DEVICE = "no.nordicsemi.android.blinky.EXTRA_DEVICE";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blinky);

		final Intent intent = getIntent();
		final ExtendedBluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
		final String deviceName = device.getName();
		final String deviceAddress = device.getAddress();

		// Setting toolbar view
		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(deviceName);
		getSupportActionBar().setSubtitle(deviceAddress);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Configure the view model
		final BlinkyViewModel viewModel = ViewModelProviders.of(this).get(BlinkyViewModel.class);
		viewModel.connect(device);

		// Set up views
		final TextView ledState = findViewById(R.id.led_state);
		final Switch led = findViewById(R.id.led_switch);
		final TextView buttonState = findViewById(R.id.button_state);
		final LinearLayout progressContainer = findViewById(R.id.progress_container);
		final TextView connectionState = findViewById(R.id.connection_state);
		final View content = findViewById(R.id.device_container);

		final ToggleButton pumpStateButton = findViewById(R.id.idPump);
		final ToggleButton fastStateButton = findViewById(R.id.idFasttrun);
		final ToggleButton slowStateButton = findViewById(R.id.idSlowtrun);
		final Button powerButton = findViewById(R.id.idPower);

		led.setOnClickListener((View view) -> {
			viewModel.toggleLED(led.isChecked());
		});
		// set onclick in pump  button, it will write cmd to device
		pumpStateButton.setOnClickListener((View view) -> {
			viewModel.togglePump(pumpStateButton.isChecked());
        });
		// set onclick in fast  button, it will write cmd to device
		fastStateButton.setOnClickListener((View view) -> {
			viewModel.toggleFast(fastStateButton.isChecked());
		});
		// set onclick in fast  button, it will write cmd to device
		slowStateButton.setOnClickListener((View view) -> {
			viewModel.toggleSlow(slowStateButton.isChecked());
		});
		// set onclick on Power  button, it will turn device off
		powerButton.setOnClickListener((View view) -> {
			viewModel.clickonPowerBut();
		});

		/*************************** add observer to Livedata *********************/
		// Create observe that for view , @deviceready
		viewModel.isDeviceReady().observe(this, deviceReady -> {
			progressContainer.setVisibility(View.GONE);
			content.setVisibility(View.VISIBLE);
		});
		// Create observer on connection state @mConnectionState (can not get the point)
		viewModel.getConnectionState().observe(this, connectionState::setText);
		// Create observer on connection if disconnect finish this UI, @mIsConnected
		viewModel.isConnected().observe(this, connected -> {
			if (!connected) {
				finish();
			}
		});
		// Create observer on LED view by @mLEDState
		viewModel.getLEDState().observe(this, isOn -> {
			ledState.setText(isOn ? R.string.turn_on : R.string.turn_off);
			led.setChecked(isOn);
		});
		// Create Observer on button by mButtonState
		viewModel.getButtonState().observe(this,
				pressed -> buttonState.setText(pressed ? R.string.button_pressed : R.string.button_released));

		// Create observer on SLOWTURN button view by @mSLOWState
		viewModel.getSLOWState().observe(this, isOn -> {
			slowStateButton.setChecked(isOn);
		});
		// Create observer on fast turn button view by @mFASTState
		viewModel.getFASTState().observe(this, isOn -> {
			fastStateButton.setChecked(isOn);
		});
		// Create observer on power turn button view by @mFmPowerState
		viewModel.getPowerState().observe(this, isOn -> {
			if(isOn)
			{powerButton.setTextColor(getResources().getColor(R.color.TurnonColor));}
			else
			{powerButton.setTextColor(getResources().getColor(R.color.background));}
		});
        // Create observer on pump button view by @mPUMPState
        viewModel.getPUMPState().observe(this, isOn -> {
            pumpStateButton.setChecked(isOn);
        });
		//
		/*************************** END of add observer to Livedata *********************/
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return false;
	}
}
