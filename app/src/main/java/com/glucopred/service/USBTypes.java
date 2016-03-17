package com.glucopred.service;

// Factory design pattern
// http://howtodoinjava.com/2012/10/23/implementing-factory-design-pattern-in-java/

public enum USBTypes {
	// OUT messages
	CLOCK_UPDATE(0x01),
	DEVICE_STATUS_REQUEST(0x02),
	PATIENT_DEVICE_SETUP(0x03),
	PATIENT_UPDATE(0x04),   // TODO remove
	//FREE(0x05), 
	PATIENT_DEVICE_UNLINK(0x06), 
	COMMAND_DATATRANSFER_START(0x07),
	COMMAND_DATATRANSFER_STOP(0x08),
	COMMAND_MODEL_FILE(0x09),
	COMMAND_MODEL_FILES_START(0x0A),
	COMMAND_MODEL_FILES_END(0x0B),
	COMMAND_DEVICE_CONFIGURATION(0x0C),
	COMMAND_CALIBRATION_BACKUP_REQUEST(0x0D),	// request for getting calibration data out of device, for backup
	COMMAND_CALIBRATION_DATA_RESTORE(0x0E),		// calibration data from server, for restore
	COMMAND_DEVICELOGIN_REQUEST(0x0F),  		// request device login credentials
	
	// IN messages
	DEVICE_STATUS(0x81),
	//FREE 0x82, 0x83, 0x84
	COMMAND_FTL_STATUS(0x85),
	COMMAND_DATASET_HISTORIC(0x86),
	COMMAND_DATASET(0x90),
	COMMAND_CALIBRATION_DATA(0x91),		// Calibration data, BSON encoded
	COMMAND_DEVICE_LOGIN(0x92),			// Device login credentials (server ID, OTP code)
	COMMAND_SCREENSHOT_DATA(0x93),		// Raw screen buffer data
	
	// SETUP messages
	SETUP_CALIBRATE_CANCEL(0xCA),
	SETUP_CALIBRATE_RESET(0xCB),
	SETUP_CLEAN(0xCC),
	SETUP_INITIALPARAMS(0xCD),
	SETUP_POWEROFF(0xCE),
	SETUP_CALIBRATE(0xCF),
	ENTER_DFU_BOOTLOADER(0xD0),
	SETUP_READ_PROTECT(0xD1),
	SETUP_CALIBRATE_IMPEDANCE(0xD2),
	SETUP_SOFTRESET(0xD3),
	SETUP_TAKE_SCREENSHOT(0xD4);
	
	private int value;

	USBTypes(int value) {
		this.value = value;
	}
	
	int value() {
		return this.value;
	}
}
