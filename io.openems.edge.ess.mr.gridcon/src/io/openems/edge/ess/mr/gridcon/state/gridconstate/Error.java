package io.openems.edge.ess.mr.gridcon.state.gridconstate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.channel.ChannelId;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.ess.mr.gridcon.GridconPCS;
import io.openems.edge.ess.mr.gridcon.IState;
import io.openems.edge.ess.mr.gridcon.StateObject;
import io.openems.edge.ess.mr.gridcon.enums.ErrorCodeChannelId0;
import io.openems.edge.ess.mr.gridcon.enums.ErrorCodeChannelId1;
import io.openems.edge.ess.mr.gridcon.enums.ErrorDoc;
import io.openems.edge.ess.mr.gridcon.enums.Mode;
import io.openems.edge.ess.mr.gridcon.enums.PControlMode;
import io.openems.edge.ess.mr.gridcon.enums.ParameterSet;

public class Error extends BaseState implements StateObject {

	
	//TODO hard restart zeit für jeden durchlauf mit einem faktor z.b. 2 multiplizieren -->
	// 1 Versuch nach 1 minute, 2. versuch nach 2 min., 3 vers. nach 4 min...usw..
	//
	
	private static final long WAITING_TIME_ERRORS = 20;
	private static final long WAITING_TIME_HARD_RESTART = 45;
	private static final float MAX_ALLOWED_DELTA_LINK_VOLTAGE = 20;
	private static final long COMMUNICATION_TIMEOUT = 60;
	private final Logger log = LoggerFactory.getLogger(Error.class);
	
	private boolean enableIPU1;
	private boolean enableIPU2;
	private boolean enableIPU3;
	private ParameterSet parameterSet;
	
	long SECONDS_TO_WAIT = WAITING_TIME_ERRORS;
	private LocalDateTime communicationBrokenSince;
	
	public Error(ComponentManager manager, String gridconPCSId, String b1Id, String b2Id, String b3Id, boolean enableIPU1, boolean enableIPU2, boolean enableIPU3, ParameterSet parameterSet, String hardRestartRelayAdress ) {	
		super(manager, gridconPCSId, b1Id, b2Id, b3Id, hardRestartRelayAdress);
		this.enableIPU1 = enableIPU1;
		this.enableIPU2 = enableIPU2;
		this.enableIPU3 = enableIPU3;
		this.parameterSet = parameterSet;
	}

	@Override
	public IState getState() {
		return io.openems.edge.ess.mr.gridcon.state.gridconstate.GridconState.ERROR;
	}

	@Override
	public IState getNextState() {
		// According to the state machine the next state can only be STOPPED, ERROR or UNDEFINED
		
		if (errorHandlingState  != null) {
			return io.openems.edge.ess.mr.gridcon.state.gridconstate.GridconState.ERROR;
		}
		
		if (isNextStateUndefined()) {
			return io.openems.edge.ess.mr.gridcon.state.gridconstate.GridconState.UNDEFINED;
		}
		
		if (isNextStateStopped()) {
			return io.openems.edge.ess.mr.gridcon.state.gridconstate.GridconState.STOPPED;
		}
		
		return io.openems.edge.ess.mr.gridcon.state.gridconstate.GridconState.ERROR;
	}

	@Override
	public void act() {
		log.info("Handle Errors!");
				
		setStringWeighting();
		setStringControlMode();
		setDateAndTime();

		if (!isBatteriesStarted()) {
			System.out.println("In error --> start batteries");
			keepSystemStopped();
			startBatteries();
			try {
				getGridconPCS().doWriteTasks();
			} catch (OpenemsNamedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
				
		//handle also link voltage too low!!
		if (getGridconPCS().isRunning() && isLinkVoltageTooLow()) {
			System.out.println("In error --> link voltage too low");
			getGridconPCS().setStop(true);
			try {
				getGridconPCS().doWriteTasks();
			} catch (OpenemsNamedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		// first step, read number of errors, then read errors while complete error list is filled
		
		//TODO sub state machine: start -> reading errors -> acknowledging --> waiting for a certain period --> finished
		
		if (getGridconPCS().isCommunicationBroken()) {
			System.out.println("Communication broken!");
			if (communicationBrokenSince == null) {
				communicationBrokenSince = LocalDateTime.now();
				System.out.println("Comm broken --> set timestamp!");
			}
			if (communicationBrokenSince.plusSeconds(COMMUNICATION_TIMEOUT).isAfter(LocalDateTime.now())) {
				System.out.println("comm broken --> in waiting time!");
				return;
			} else {
				System.out.println("comm broken --> hard reset!");
				communicationBrokenSince = null;
				errorHandlingState = ErrorHandlingState.HARD_RESTART;
			}
		}
		
		if (errorHandlingState == null) {
			errorHandlingState = ErrorHandlingState.START;
		}
		
		switch (errorHandlingState) {
		case START:
			doStartErrorHandling();
			break;
		case READING_ERRORS:
			doReadErrors();
			break;
		case ACKNOWLEDGE:
			doAcknowledge();
			break;
		case WAITING:
			doWait();
			break;
		case FINISHED:
			finishing();
			break;
		case HARD_RESTART:
			doHardRestart();
			break;
		}		
		try {
			getGridconPCS().doWriteTasks();
		} catch (OpenemsNamedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int hardRestartCnt = 0;
	private void doHardRestart() {
		System.out.println(" ---> HARD RESET <---");
		if (hardRestartCnt < 10) {
			System.out.println(" ---> HARD RESET counting <---");
			hardRestartCnt++;
			setHardRestartRelay(true);
		} else {
			System.out.println(" ---> HARD RESET get to waiting<---");
			hardRestartCnt = 0;
			setHardRestartRelay(false);
			errorHandlingState = ErrorHandlingState.WAITING;
			SECONDS_TO_WAIT = WAITING_TIME_HARD_RESTART;
		}		
	}

	private void keepSystemStopped() {		
		System.out.println("Keep system stopped!");
		getGridconPCS().setEnableIPU1(false);
		getGridconPCS().setEnableIPU2(false);
		getGridconPCS().setEnableIPU3(false);
		getGridconPCS().disableDCDC();
		
		getGridconPCS().setStop(true);
		getGridconPCS().setPlay(false);
		getGridconPCS().setAcknowledge(false);
		
		getGridconPCS().setSyncApproval(true);
		getGridconPCS().setBlackStartApproval(false);
		getGridconPCS().setModeSelection(Mode.CURRENT_CONTROL);
		getGridconPCS().setU0(BaseState.ONLY_ON_GRID_VOLTAGE_FACTOR);
		getGridconPCS().setF0(BaseState.ONLY_ON_GRID_FREQUENCY_FACTOR);
		getGridconPCS().setPControlMode(PControlMode.ACTIVE_POWER_CONTROL);
		getGridconPCS().setQLimit(GridconPCS.Q_LIMIT);
		getGridconPCS().setDcLinkVoltage(GridconPCS.DC_LINK_VOLTAGE_SETPOINT);
		
		getGridconPCS().setParameterSet(parameterSet);				
		float maxPower = GridconPCS.MAX_POWER_PER_INVERTER;
		if (enableIPU1) {
			getGridconPCS().setPMaxChargeIPU1(maxPower);
			getGridconPCS().setPMaxDischargeIPU1(-maxPower);
		}
		if (enableIPU2) {
			getGridconPCS().setPMaxChargeIPU2(maxPower);
			getGridconPCS().setPMaxDischargeIPU2(-maxPower);
		}
		if (enableIPU3) {
			getGridconPCS().setPMaxChargeIPU3(maxPower);
			getGridconPCS().setPMaxDischargeIPU3(-maxPower);
		}		
}
	
	private boolean isLinkVoltageTooLow() {
		return GridconPCS.DC_LINK_VOLTAGE_SETPOINT - getGridconPCS().getDcLinkPositiveVoltage() > MAX_ALLOWED_DELTA_LINK_VOLTAGE;
	}
	
	private void doStartErrorHandling() {
		System.out.println("doStartErrorHandling");
		// Error Count = anzahl der Fehler
		// Error code = aktueller fehler im channel
		// error code feedback -> channel fürs rückschreiben der fehler damit im error code der nächste auftaucht
		
		errorCollection = new ArrayList<Integer>();
		errorCount = getGridconPCS().getErrorCount();
		errorHandlingState = ErrorHandlingState.READING_ERRORS;
		//getGridconPCS().setStop(true);
		keepSystemStopped();		
	}
	
	private void doReadErrors() {
		System.out.println("doReadErrors");
		int currentErrorCode = getGridconPCS().getErrorCode();
		if (!errorCollection.contains(currentErrorCode)) {
			errorCollection.add(currentErrorCode);
			getGridconPCS().setErrorCodeFeedback(currentErrorCode);
		} else {
			getGridconPCS().setErrorCodeFeedback(currentErrorCode);
		}
		
		if (errorCollection.size() >= errorCount) {
			errorHandlingState = ErrorHandlingState.ACKNOWLEDGE;
			//write errors
			printErrors(errorCollection);
		}
	}
	
	private void printErrors(Collection<Integer> errorCollection) {
		for (int i : errorCollection) {
			printError(i);
		}		
	}

	private void printError(int errorCode) {
		for (ErrorCodeChannelId0 id : ErrorCodeChannelId0.values()) {			
			printErrorIfCorresponding(errorCode, id);
		}
		for (ErrorCodeChannelId1 id : ErrorCodeChannelId1.values()) {
			printErrorIfCorresponding(errorCode, id);
		}
	}

	private void printErrorIfCorresponding(int errorCode, ChannelId id) {
		ErrorDoc errorDoc = (ErrorDoc) id.doc();
		if (errorDoc.getCode() == errorCode) {
			System.out.println(errorDoc.getText());
		}
	}
	
	
	

	private void doAcknowledge() {
		System.out.println("doAcknowledge");
		errorsAcknowledged = LocalDateTime.now();
		errorHandlingState = ErrorHandlingState.WAITING;
		SECONDS_TO_WAIT = WAITING_TIME_ERRORS;
		
		getGridconPCS().setEnableIPU1(false);
		getGridconPCS().setEnableIPU2(false);
		getGridconPCS().setEnableIPU3(false);
		getGridconPCS().disableDCDC();
		
		getGridconPCS().setStop(true);
		getGridconPCS().setPlay(false);
		getGridconPCS().setAcknowledge(true);
		
		getGridconPCS().setSyncApproval(true);
		getGridconPCS().setBlackStartApproval(false);
		getGridconPCS().setModeSelection(Mode.CURRENT_CONTROL);
		getGridconPCS().setU0(BaseState.ONLY_ON_GRID_VOLTAGE_FACTOR);
		getGridconPCS().setF0(BaseState.ONLY_ON_GRID_FREQUENCY_FACTOR);
		getGridconPCS().setPControlMode(PControlMode.ACTIVE_POWER_CONTROL);
		getGridconPCS().setQLimit(GridconPCS.Q_LIMIT);
		getGridconPCS().setDcLinkVoltage(GridconPCS.DC_LINK_VOLTAGE_SETPOINT);
		
		getGridconPCS().setParameterSet(parameterSet);				
		float maxPower = GridconPCS.MAX_POWER_PER_INVERTER;
		if (enableIPU1) {
			getGridconPCS().setPMaxChargeIPU1(maxPower);
			getGridconPCS().setPMaxDischargeIPU1(-maxPower);
		}
		if (enableIPU2) {
			getGridconPCS().setPMaxChargeIPU2(maxPower);
			getGridconPCS().setPMaxDischargeIPU2(-maxPower);
		}
		if (enableIPU3) {
			getGridconPCS().setPMaxChargeIPU3(maxPower);
			getGridconPCS().setPMaxDischargeIPU3(-maxPower);
		}		
	}

	private void finishing() {
		System.out.println("finishing");
		//reset all maps etc.
		
		errorCount = null;
		errorCollection = null; 
		errorHandlingState = null;
		errorsAcknowledged = null;
		
	}	
	private void doWait() {
		System.out.println("doWait");
		
		if (errorsAcknowledged.plusSeconds(SECONDS_TO_WAIT).isBefore(LocalDateTime.now())) {
			
			if (getGridconPCS().isError()) {
				System.out.println("Gridcon has still errors.... :-(  start from the beginning");
				finishing(); // to reset all maps etc...				
			}
			
			errorHandlingState = ErrorHandlingState.FINISHED;
		} else {
			System.out.println("we are still waiting");
		}
		
	}

	Integer errorCount = null;
	private Collection<Integer> errorCollection = null; 
	private ErrorHandlingState errorHandlingState = null;
	LocalDateTime errorsAcknowledged = null;
	
	private enum ErrorHandlingState {
		START,
		READING_ERRORS,
		ACKNOWLEDGE,
		WAITING,
		FINISHED, HARD_RESTART		
	}
}