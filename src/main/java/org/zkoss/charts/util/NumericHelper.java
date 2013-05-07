/** NumericHelper.java.

	Purpose:
		
	Description:
		
	History:
		5:22:34 PM Apr 26, 2013, Created by jumperchen

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
*/
package org.zkoss.charts.util;

/**
 * NumericImpl contains logic for numeric data. This implementation is referred to YUI's NumericImpl.js which license under
 * BSD License. - http://yuilibrary.com/license/
 * @author jumperchen
 *
 */
public class NumericHelper {

	public static double roundToNearest(double number, double nearest) {
		double roundedNumber = Math
				.round(roundToPrecision(number / nearest, 10)) * nearest;
		return roundToPrecision(roundedNumber, 10);
	}

	public static double roundToPrecision(double number, int precision) {
		double decimalPlaces = Math.pow(10, precision);
		return Math.round(decimalPlaces * number) / decimalPlaces;
	}

	public static double getMinimumUnit(double max, double min, int units) {
		return getNiceNumber(Math.ceil((max - min) / units));
	}

	public static double getNiceNumber(double roundingUnit) {
		double tempMajorUnit = roundingUnit;
		double order = Math.ceil(Math.log(tempMajorUnit) * 0.4342944819032518);
		double roundedMajorUnit = Math.pow(10, order);
		double roundedDiff;

		if (roundedMajorUnit / 2 >= tempMajorUnit) {
			roundedDiff = Math.floor((roundedMajorUnit / 2 - tempMajorUnit)
					/ (Math.pow(10, order - 1) / 2));
			tempMajorUnit = roundedMajorUnit / 2 - roundedDiff
					* Math.pow(10, order - 1) / 2;
		} else {
			tempMajorUnit = roundedMajorUnit;
		}
		if (tempMajorUnit >= 0) {
			return tempMajorUnit;
		}
		return roundingUnit;
	}

	public static double roundDownToNearest(double number, double nearest) {
		return Math.floor(roundToPrecision(number / nearest, 10))
				* nearest;
	}

	public static double roundUpToNearest(double number, double nearest) {
		return Math.ceil(roundToPrecision(number / nearest, 10)) * nearest;
	}

	public static double[] roundMinAndMax(double min, double max, int tickCount) {
		double roundingUnit;
		boolean minGreaterThanZero = min >= 0;
		boolean maxGreaterThanZero = max > 0;
		int topTicks;
		int botTicks;
		double tempMax;
		double tempMin;
		int units = tickCount - 1;
		boolean alwaysShowZero = true;

		roundingUnit = getMinimumUnit(max, min, units);
		if (minGreaterThanZero && maxGreaterThanZero) {
			if ((alwaysShowZero || min < roundingUnit)) {
				min = 0;
				roundingUnit = getMinimumUnit(max, min, units);
			} else {
				min = roundDownToNearest(min, roundingUnit);
			}

			max = roundUpToNearest(max, roundingUnit);
		} else if (maxGreaterThanZero && !minGreaterThanZero) {
			if (alwaysShowZero) {
				topTicks = (int) Math.round(units / ((-1 * min) / max + 1));
				topTicks = Math.max(Math.min(topTicks, units - 1), 1);
				botTicks = units - topTicks;
				tempMax = Math.ceil(max / topTicks);
				tempMin = Math.floor(min / botTicks) * -1;

				roundingUnit = Math.max(tempMax, tempMin);
				roundingUnit = getNiceNumber(roundingUnit);
				max = roundingUnit * topTicks;
				min = roundingUnit * botTicks * -1;
			} else {

				min = roundDownToNearest(min, roundingUnit);
				max = roundUpToNearest(max, roundingUnit);
			}
		} else {

			min = max - (roundingUnit * units);

		}
		return new double[] { min, max };
	}
}
