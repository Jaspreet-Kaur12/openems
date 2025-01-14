package io.openems.edge.evcs.core;

import java.util.Map;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.filter.DisabledRampFilter;
import io.openems.edge.common.filter.RampFilter;
import io.openems.edge.evcs.api.EvcsPower;

@Designate(ocd = Config.class, factory = false)
@Component(//
		name = EvcsPowerComponent.SINGLETON_SERVICE_PID, //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.OPTIONAL, //
		property = { //
				"enabled=true", //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_WRITE, //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_WRITE //
		})
public class EvcsPowerComponent extends AbstractOpenemsComponent implements OpenemsComponent, EvcsPower {

	public final static String SINGLETON_SERVICE_PID = "Evcs.SlowPowerIncreaseFilter";
	public final static String SINGLETON_COMPONENT_ID = "_evcsSlowPowerIncreaseFilter";

	@Reference
	private ConfigurationAdmin cm;

	private double increasingRate = RampFilter.DEFAULT_INCREASE_RATE;
	private RampFilter rampFilter;

	public EvcsPowerComponent() {
		super(//
				OpenemsComponent.ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Map<String, Object> properties, Config config) {
		super.activate(context, SINGLETON_COMPONENT_ID, SINGLETON_SERVICE_PID, true);
		if (OpenemsComponent.validateSingleton(this.cm, SINGLETON_SERVICE_PID, SINGLETON_COMPONENT_ID)) {
			return;
		}
		this.updateConfig(config);
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Modified
	void modified(ComponentContext context, Config config) throws OpenemsNamedException {
		super.modified(context, SINGLETON_COMPONENT_ID, SINGLETON_SERVICE_PID, true);
		if (OpenemsComponent.validateSingleton(this.cm, SINGLETON_SERVICE_PID, SINGLETON_COMPONENT_ID)) {
			return;
		}
		this.updateConfig(config);
	}

	private void updateConfig(Config config) {
		if (config.enableSlowIncrease()) {
			this.rampFilter = new RampFilter(this.increasingRate);
		} else {
			this.rampFilter = new DisabledRampFilter();
		}
	}

	@Override
	public RampFilter getRampFilter() {
		return this.rampFilter;
	}
}
