package io.openems.edge.battery.soltaro.single.versiona;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.edge.battery.soltaro.common.enums.BatteryState;

@ObjectClassDefinition(//
		name = "BMS Soltaro Single Rack Version A", //
		description = "Implements the Soltaro battery rack system.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "bms0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Modbus-ID", description = "ID of Modbus bridge.")
	String modbus_id() default "modbus0";

	@AttributeDefinition(name = "Modbus Unit-ID", description = "The Unit-ID of the Modbus device.")
	int modbusUnitId() default 1;

	@AttributeDefinition(name = "Error Level 2 Delay", description = "Sets the delay time in seconds how long the system should be stopped after an error level 2 has occurred")
	int errorLevel2Delay() default 600;
	
	@AttributeDefinition(name = "Max Start Time", description = "Max Time in seconds allowed for starting the system")
	int maxStartTime() default 30;
	
	@AttributeDefinition(name = "Pending Tolerance", description = "time in seconds, that is waited if system status cannot be determined e.g. in case of reading errors")
	int pendingTolerance() default 15;
	
	@AttributeDefinition(name = "Max Start Attempts", description = "Sets the counter how many time the system should try to start")
	int maxStartAppempts() default 5;
	
	@AttributeDefinition(name = "Start Not Successful Delay Time", description = "Sets the delay time in seconds how long the system should be stopped if it was not able to start")
	int startUnsuccessfulDelay() default 3600;
	
	@AttributeDefinition(name = "Minimal Cell Voltage Millivolt", description = "Minimal cell voltage in milli volt when system does not allow further discharging")
	int minimalCellVoltage() default 2800;
	
	@AttributeDefinition(name = "Capacity [kWh]", description = "The capacity of the Battery Rack.")
	int capacity() default 50;

	@AttributeDefinition(name = "Battery state", description = "Switches the battery into the given state, if default is used, battery state is set automatically")
	BatteryState batteryState() default BatteryState.DEFAULT;

	@AttributeDefinition(name = "Modbus target filter", description = "This is auto-generated by 'Modbus-ID'.")
	String Modbus_target() default "(enabled=true)";

	String webconsole_configurationFactory_nameHint() default "BMS Soltaro Single Rack Version A [{id}]";
}