package io.openems.edge.battery.protection;

import io.openems.edge.battery.protection.force.ForceCharge;
import io.openems.edge.common.component.ClockProvider;
import io.openems.edge.common.linecharacteristic.PolyLine;

public class DischargeMaxCurrentHandler extends AbstractMaxCurrentHandler {

	public static class Builder extends AbstractMaxCurrentHandler.Builder<Builder> {

		private ForceCharge.Params forceChargeParams = null;

		protected Builder(ClockProvider clockProvider, int initialBmsMaxEverChargeCurrent) {
			super(clockProvider, initialBmsMaxEverChargeCurrent);
		}

		/**
		 * Configure 'Force Charge' parameters.
		 * 
		 * @param startChargeBelowCellVoltage    start force charge if minCellVoltage is
		 *                                       below this value, e.g. 2850
		 * @param chargeBelowCellVoltage         force charge as long as minCellVoltage
		 *                                       is below this value, e.g. 2910
		 * @param blockDischargeBelowCellVoltage after 'force charge', block discharging
		 *                                       as long as minCellVoltage is below this
		 *                                       value, e.g. 3000
		 * @return {@link Builder}
		 */
		public Builder setForceCharge(int startChargeBelowCellVoltage, int chargeBelowCellVoltage,
				int blockDischargeBelowCellVoltage) {
			this.forceChargeParams = new ForceCharge.Params(startChargeBelowCellVoltage, chargeBelowCellVoltage,
					blockDischargeBelowCellVoltage);
			return this;
		}

		public Builder setForceCharge(ForceCharge.Params forceChargeParams) {
			this.forceChargeParams = forceChargeParams;
			return this;
		}

		public DischargeMaxCurrentHandler build() {
			return new DischargeMaxCurrentHandler(this.clockProvider, this.initialBmsMaxEverCurrent,
					this.voltageToPercent, this.temperatureToPercent, this.maxIncreasePerSecond,
					new ForceCharge(this.forceChargeParams));
		}

		@Override
		protected Builder self() {
			return this;
		}
	}

	/**
	 * Create a {@link DischargeMaxCurrentHandler} builder.
	 * 
	 * @param clockProvider                     a {@link ClockProvider}
	 * @param initialBmsMaxEverDischargeCurrent the (estimated) maximum allowed
	 *                                          discharge current. This is used as a
	 *                                          reference for percentage values. If
	 *                                          during runtime a higher value is
	 *                                          provided, that one is taken from
	 *                                          then on.
	 * @return a {@link Builder}
	 */
	public static Builder create(ClockProvider clockProvider, int initialBmsMaxEverChargeCurrent) {
		return new Builder(clockProvider, initialBmsMaxEverChargeCurrent);
	}

	protected DischargeMaxCurrentHandler(ClockProvider clockProvider, int initialBmsMaxEverDischargeCurrent,
			PolyLine voltageToPercent, PolyLine temperatureToPercent, double maxIncreasePerSecond,
			ForceCharge forceCharge) {
		super(clockProvider, initialBmsMaxEverDischargeCurrent, voltageToPercent, temperatureToPercent,
				maxIncreasePerSecond, forceCharge);
	}
}