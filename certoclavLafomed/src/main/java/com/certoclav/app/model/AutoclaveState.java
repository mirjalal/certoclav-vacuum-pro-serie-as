package com.certoclav.app.model;

public enum AutoclaveState {
	LOCKED,//<=> no user logged in
	NOT_RUNNING,
	PREPARE_TO_RUN,
	RUNNING,
	RUN_CANCELED,
	PROGRAM_FINISHED, //erst anzeigen, wenn wasser bei türöffnung nicht siedet
	WAITING_FOR_CONFIRMATION
}
