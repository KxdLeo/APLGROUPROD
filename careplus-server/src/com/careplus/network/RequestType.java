package com.careplus.network;

public enum RequestType {
	
	//Auth
	LOGIN,
	
	//Patinet actions, gets called in Request 
	SUBMIT_COMPLAINT,
	VIEW_MY_COMPLAINTS,
	VIEW_MY_APPOINTMENTS,
    VIEW_MY_PAYMENTS,

    // Receptionist actions
    GET_ALL_COMPLAINTS,
    RESPOND_TO_COMPLAINT,
    ASSIGN_STAFF,

    // Doctor actions
    GET_ASSIGNED_PATIENTS,
    ADD_DIAGNOSIS,
    SCHEDULE_FOLLOWUP,

    // Nurse actions
    GET_ASSIGNED_CASES,
    RECORD_VITALS,

    // Live chat
    SEND_CHAT_MESSAGE,
    CHAT_MESSAGE_PUSH   // server pushes this to the recipient
}
	


