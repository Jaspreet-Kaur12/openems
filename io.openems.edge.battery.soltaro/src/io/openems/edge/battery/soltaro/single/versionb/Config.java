package io.openems.edge.battery.soltaro.single.versionb;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.edge.battery.soltaro.common.enums.ModuleType;
import io.openems.edge.common.startstop.StartStopConfig;

@ObjectClassDefinition(//
		name = "BMS Soltaro Single Rack Version B", //
		description = "Implements the Soltaro battery rack system.")
public @interface Config {

	/**
	 * Return the id.
	 * 
	 * @return id
	 */
	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "bms0";

	/**
	 * Return the alias.
	 * 
	 * @return alias
	 */
	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	/**
	 * Return the enabled.
	 * 
	 * @return enabled
	 */
	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	/**
	 * Return the modbus_id.
	 * 
	 * @return modbus_id
	 */
	@AttributeDefinition(name = "Modbus-ID", description = "ID of Modbus bridge.")
	String modbus_id() default "modbus0";

	/**
	 * Return the modbusUnitId.
	 * 
	 * @return modbusUnitId
	 */
	@AttributeDefinition(name = "Modbus Unit-ID", description = "The Unit-ID of the Modbus device.")
	int modbusUnitId() default 11;

	/**
	 * Gets the StartStopConfig.
	 * 
	 * @return StartStopConfig
	 */
	@AttributeDefinition(name = "Start/stop behaviour?", description = "Should this Component be forced to start or stop?")
	StartStopConfig startStop() default StartStopConfig.AUTO;

	/**
	 * Return the moduleType.
	 * 
	 * @return moduleType
	 */
	@AttributeDefinition(name = "Module type", description = "The type of modules in the rack")
	ModuleType moduleType() default ModuleType.MODULE_3_5_KWH;

	/**
	 * Return the errorLevel2Delay.
	 * 
	 * @return errorLevel2Delay
	 */
	@AttributeDefinition(name = "Error Level 2 Delay", description = "Sets the delay time in seconds how long the system should be stopped after an error level 2 has occurred")
	int errorLevel2Delay() default 600;

	/**
	 * Return the maxStartAppempts.
	 * 
	 * @return maxStartAppempts
	 */
	@AttributeDefinition(name = "Max Start Attempts", description = "Sets the counter how many time the system should try to start")
	int maxStartAppempts() default 5;

	/**
	 * Return the maxStartTime.
	 * 
	 * @return maxStartTime
	 */
	@AttributeDefinition(name = "Max Start Time", description = "Max Time in seconds allowed for starting the system")
	int maxStartTime() default 30;

	/**
	 * Return the startUnsuccessfulDelay.
	 * 
	 * @return startUnsuccessfulDelay
	 */
	@AttributeDefinition(name = "Start Not Successful Delay Time", description = "Sets the delay time in seconds how long the system should be stopped if it was not able to start")
	int startUnsuccessfulDelay() default 3600;

	/**
	 * Return the watchdog.
	 * 
	 * @return watchdog
	 */
	@AttributeDefinition(name = "Watchdog", description = "Watchdog timeout in seconds")
	int watchdog() default 60;

	/**
	 * Return the pendingTolerance.
	 * 
	 * @return pendingTolerance
	 */
	@AttributeDefinition(name = "Pending Tolerance", description = "time in seconds, that is waited if system status cannot be determined e.g. in case of reading errors")
	int pendingTolerance() default 15;

	/**
	 * Return the soCLowAlarm.
	 * 
	 * @return soCLowAlarm
	 */
	@AttributeDefinition(name = "SoC Low Alarm", description = "Sets the value for BMS SoC protection (0..100)", min = "0", max = "100")
	int SoCLowAlarm() default 0;

	/**
	 * Return the minimalCellVoltage.
	 * 
	 * @return minimalCellVoltage
	 */
	@AttributeDefinition(name = "Minimal Cell Voltage Millivolt", description = "Minimal cell voltage in milli volt when system does not allow further discharging")
	int minimalCellVoltage() default 2800;

	/**
	 * Return the reduceTasks.
	 * 
	 * @return reduceTasks
	 */
	@AttributeDefinition(name = "Reduce tasks", description = "Reduces read and write tasks to avoid errors")
	boolean ReduceTasks() default false;

	/**
	 * Return the Modbus_target.
	 * 
	 * @return Modbus_target
	 */
	@AttributeDefinition(name = "Modbus target filter", description = "This is auto-generated by 'Modbus-ID'.")
	String Modbus_target() default "(enabled=true)";

	/**
	 * Return the webconsole_configurationFactory_nameHint.
	 * 
	 * @return webconsole_configurationFactory_nameHint
	 */
	String webconsole_configurationFactory_nameHint() default "BMS Soltaro Single Rack Version B [{id}]";
}